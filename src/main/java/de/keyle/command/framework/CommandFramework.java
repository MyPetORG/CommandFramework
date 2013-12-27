/*
 * This file is part of Keyle's CommandFramework
 *
 * Copyright (C) 2011-2013 Keyle
 * Keyle's CommandFramework is licensed under the GNU Lesser General Public License.
 *
 * Keyle's CommandFramework is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Keyle's CommandFramework is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package de.keyle.command.framework;

import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.SimplePluginManager;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.Map.Entry;

import static java.util.AbstractMap.SimpleEntry;

public class CommandFramework {
    private Map<String, Entry<Method, Object>> commandMap = new HashMap<String, Entry<Method, Object>>();
    private CommandMap bukkitCommandMap;
    private Plugin plugin;

    /**
     * Initializes the command framework and sets up the command maps
     *
     * @param plugin The {@link org.bukkit.plugin.java.JavaPlugin} the command is registered for
     */
    public CommandFramework(Plugin plugin) {
        this.plugin = plugin;
        if (plugin.getServer().getPluginManager() instanceof SimplePluginManager) {
            SimplePluginManager pluginManager = (SimplePluginManager) plugin.getServer().getPluginManager();
            try {
                Field field = SimplePluginManager.class.getDeclaredField("commandMap");
                field.setAccessible(true);
                bukkitCommandMap = (CommandMap) field.get(pluginManager);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (SecurityException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Handles commands. Used in the onCommand method in your JavaPlugin class
     *
     * @param sender The {@link org.bukkit.command.CommandSender} parsed from onCommand
     * @param label  The label parsed from onCommand
     * @param cmd    The {@link org.bukkit.command.Command} parsed from onCommand
     * @param args   The arguments parsed from onCommand
     * @return Always returns true
     */
    public boolean handleCommand(CommandSender sender, String label, org.bukkit.command.Command cmd, String[] args) {
        StringBuilder commandName = new StringBuilder();
        List<String> arguments = new ArrayList<String>();
        arguments.add(label);
        arguments.addAll(Arrays.asList(args));

        String lastCommand = "";
        int removeCount = 1;
        for (int i = 0; i < arguments.size(); i++) {
            String arg = arguments.get(i);
            if (commandName.length() != 0) {
                commandName.append(".");
            }
            commandName.append(arg);
            if (commandMap.containsKey(commandName.toString())) {
                lastCommand = commandName.toString();
                removeCount = i + 1;
            }
        }
        if (!lastCommand.isEmpty()) {
            for (int i = 0; i < removeCount; i++) {
                arguments.remove(0);
            }
            Entry<Method, Object> entry = commandMap.get(lastCommand);
            Command command = entry.getKey().getAnnotation(Command.class);
            if (!sender.hasPermission(command.permission())) {
                sender.sendMessage(command.noPerm());
                return true;
            }
            try {
                entry.getKey().invoke(entry.getValue(), new CommandArgs(sender, cmd, Collections.unmodifiableList(arguments)));
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
            return true;
        }
        return true;
    }

    /**
     * Registers the commands and tab completers from the given object
     *
     * @param obj The object the command and tab completers are in
     */
    public void registerCommands(Object obj) {
        for (Method method : obj.getClass().getMethods()) {
            if (method.getAnnotation(Command.class) != null) {
                Command command = method.getAnnotation(Command.class);
                if (method.getParameterTypes().length > 1 || method.getParameterTypes()[0] != CommandArgs.class) {
                    printMessage("Unable to register command \"" + method.getName() + "\". Unexpected method arguments");
                    continue;
                }
                registerCommand(command, command.name(), method, obj);
                for (String alias : command.aliases()) {
                    registerCommand(command, alias, method, obj);
                }
            } else if (method.getAnnotation(Completer.class) != null) {
                Completer completerInfo = method.getAnnotation(Completer.class);
                if (method.getParameterTypes().length > 1 || method.getParameterTypes().length == 0 || method.getParameterTypes()[0] != CommandArgs.class) {
                    printMessage("Unable to register tab completer " + method.getName() + ". Unexpected method arguments");
                    continue;
                }
                if (method.getReturnType() != List.class) {
                    printMessage("Unable to register tab completer " + method.getName() + ". Unexpected return type");
                    continue;
                }
                registerCompleter(completerInfo.name(), method, obj);
                for (String alias : completerInfo.aliases()) {
                    registerCompleter(alias, method, obj);
                }
            }
        }
    }

    private void registerCommand(Command commandInfo, String label, Method method, Object obj) {
        commandMap.put(label.toLowerCase(), new SimpleEntry<Method, Object>(method, obj));
        String commandName = label.split("\\.")[0].toLowerCase();

        org.bukkit.command.Command command = bukkitCommandMap.getCommand(commandName);
        if (command == null) {
            command = new BukkitCommand(commandName, plugin);
            bukkitCommandMap.register(plugin.getName(), command);
        }
        if (!commandInfo.description().isEmpty() && commandName.equals(label)) {
            command.setDescription(commandInfo.description());
        }
        if (!commandInfo.usage().isEmpty() && commandName.equals(label)) {
            command.setUsage(commandInfo.usage());
        }
    }

    private void registerCompleter(String commandName, Method method, Object obj) {
        String origCommandName = commandName;
        commandName = commandName.split("\\.")[0].toLowerCase();
        org.bukkit.command.Command command = bukkitCommandMap.getCommand(commandName);

        if (bukkitCommandMap.getCommand(commandName) == null) {
            command = new BukkitCommand(commandName, plugin);
            bukkitCommandMap.register(plugin.getName(), command);
        }
        if (command instanceof BukkitCommand) {
            BukkitCommand bukkitCommand = (BukkitCommand) command;
            if (bukkitCommand.completer == null) {
                bukkitCommand.completer = new BukkitCompleter();
            }
            bukkitCommand.completer.addCompleter(origCommandName, method, obj);
        } else if (command instanceof PluginCommand) {
            try {
                PluginCommand pluginCommand = (PluginCommand) command;
                Field field = pluginCommand.getClass().getDeclaredField("completer");
                field.setAccessible(true);
                if (field.get(pluginCommand) == null) {
                    BukkitCompleter completer = new BukkitCompleter();
                    completer.addCompleter(origCommandName, method, obj);
                    field.set(pluginCommand, completer);
                } else if (field.get(pluginCommand) instanceof BukkitCompleter) {
                    BukkitCompleter completer = (BukkitCompleter) field.get(pluginCommand);
                    completer.addCompleter(origCommandName, method, obj);
                } else {
                    printMessage("Unable to register tab completer " + method.getName() + ". A tab completer is already registered for this command!");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * Method that can be overridden to set an output for messages thrown by this framework
     *
     * @param message The message to print
     */
    @SuppressWarnings("unused")
    public void printMessage(String message) {
    }
}