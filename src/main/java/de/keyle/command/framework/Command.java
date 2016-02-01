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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Command {
    /**
     * The name of the command.
     *
     * @return The name
     */
    String name();

    /**
     * Gets the required permission of the command
     *
     * @return The permission
     */
    String permission() default "";

    /**
     * The message sent to the player when they do not have permission to execute it
     *
     * @return The message
     */
    String noPerm() default "You do not have permission to perform that action";

    /**
     * A list of alternate names that the command is executed under.
     *
     * @return Array of all aliases
     */
    String[] aliases() default {};

    /**
     * The description that will appear in /help of the command
     *
     * @return The description of this command
     */
    String description() default "";

    /**
     * The usage that will appear in /help
     *
     * @return The usage description of this command
     */
    String usage() default "";
}