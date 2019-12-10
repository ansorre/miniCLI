/*
 * Copyright 2019 Antonio Sorrentini
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */


package me.as.lib.minicli;


import java.io.File;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.io.File.pathSeparator;


@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface CLIArgument
{

 /**
  * Name of the argument when read from a config file.
  * When null or empty this argument is not allowed in a config file
  */
 String nameInConfigFiles() default "";

 /**
  * Position of the argument.
  *
  * <p>
  * If you define multiple single value properties to bind to arguments,
  * they should have index=0, index=1, index=2, ... and so on.
  *
  * <p>
  * Multi value properties bound to arguments must always be the last entry.
  */
 int index() default 0;

 /**
  * Help string used to display the usage screen.
  *
  * <p>
  * This parameter works in two ways. For a simple use, you can just encode the human-readable help string directly, and that will be used as the
  * message.
  * <p>
  * Otherwise you can set this to a single word starting with § in which case if a Localizer is provided this is passed (to the CommandLineHandler
  * method prepare) to it to get the actual text which will then be used as the message.
  */
 String usage() default "";

 /**
  * When the option takes an operand, the usage screen will show something like this:
  * <pre>
  * -x OPERAND  : blah blah blah
  * </pre>
  * With this parameter you give the value for OPERAND.
  * <p>
  * Like for 'usage' this parameter works in two ways. For a simple use, you can just encode the human-readable string directly. Otherwise you can
  * set this to a single word starting with § in which case if a Localizer is provided this is passed (to the CommandLineHandler method prepare) to
  * it to get the actual text which will then be used me.as the message.
  *
  * <p>
  * If left unspecifiied, this value is infered from the type of the option.
  * If the option shoud have no operand you set the option type to NoOperand.
  * Eg.:: @CLIOption(name="--help") NoOperand help;
  */
 String operand() default "";

 /**
  * If documented is false this argument will not be shown in help
  */
 boolean documented() default true;

 /**
  * Specify if the argument is mandatory.
  */
 boolean required() default false;

 /**
  * If the argument is mandatory and still the user didn't pass it, if missing is not empty will be shown
  */
 String missing() default "";

 /**
  * Specify the {@link CLIOptionHandler} that processes the command line arguments.
  *
  * <p>
  * The default value {@link CLIOptionHandler} indicates that the {@link CLIOptionHandler} will be infered from the type of the field/method where a
  * {@link CLIOption} annotation is placed.
  *
  * <p>
  * If this annotation element is used, it overrides the inference and determines the handler to be used. This is convenient for defining a
  * non-standard option parsing semantics.
  *
  * <h3>Example</h3>
  * <pre>
  * &#64;CLIOption(name="-cp", handler=ClasspathCLIOptionHandler.class) List<String> classpath;
  * </pre>
  */
 Class<? extends CLIArgumentHandler> handlerClass() default CLIArgumentHandler.class;

 /**
  * When true the program execution terminastes after the CLIOptionHandler processing
  */
 boolean execAndExit() default false;

 /**
  * When the option is multi-valued (its type is an array or a collection) this is used (by CLIOptionHandler) to determine which is the string that
  * separates the values.
  * When empty an exception is thrown or if this Annotation is applied to the last CLIArgument successive arguments will be added to this
  */
 String separator() default ",";


}
