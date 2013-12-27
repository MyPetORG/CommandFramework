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

import java.util.List;

public class CommandArgs {
    private final CommandSender sender;
    private final Command command;
    private final List<String> args;

    protected CommandArgs(CommandSender sender, Command command, List<String> args) {
        this.sender = sender;
        this.command = command;
        this.args = args;
    }

    /**
     * Gets the sender of the command
     *
     * @return the {@link CommandSender}
     */
    public CommandSender getSender() {
        return sender;
    }

    /**
     * Gets the Bukkit command object
     *
     * @return The Bukkit {@link Command}
     */
    public Command getCommand() {
        return command;
    }

    /**
     * Gets all the arguments.
     *
     * @return A list of all arguments
     */
    public List<String> getArgs() {
        return args;
    }
}