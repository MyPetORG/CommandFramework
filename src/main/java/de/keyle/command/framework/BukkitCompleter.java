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

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.Map.Entry;

public class BukkitCompleter implements TabCompleter {
    private static List<String> emptyList = new ArrayList<String>();
    private Map<String, Entry<Method, Object>> completers = new HashMap<String, Entry<Method, Object>>();

    public void addCompleter(String label, Method m, Object obj) {
        completers.put(label, new AbstractMap.SimpleEntry<Method, Object>(m, obj));
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        StringBuilder commandName = new StringBuilder();
        List<String> arguments = new ArrayList<String>();
        arguments.add(label);
        arguments.addAll(Arrays.asList(args));
        if (args.length > 0) {
            arguments.remove(args.length);
        }

        String lastCommand = "";
        int removeCount = 1;
        for (int i = 0; i < arguments.size(); i++) {
            String arg = arguments.get(i);
            if (commandName.length() != 0) {
                commandName.append(".");
            }
            commandName.append(arg);
            if (completers.containsKey(commandName.toString())) {
                lastCommand = commandName.toString();
                removeCount = i + 1;
            }
        }
        if (!lastCommand.isEmpty()) {
            for (int i = 0; i < removeCount; i++) {
                arguments.remove(0);
            }
            Entry<Method, Object> entry = completers.get(lastCommand);
            try {
                return (List<String>) entry.getKey().invoke(entry.getValue(), new CommandArgs(sender, command, arguments));
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        return emptyList;
    }
}