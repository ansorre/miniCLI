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


public class Settings
{
 enum Type
 {
  argument,
  option
 }

 public Type type;

 public int index;
 public String name;
 public String aliases[];
 public String configFileName;
 public String usage;
 public String operand;
 public boolean documented;
 public int helpOrder;
 public boolean required;
 public String missing;
 public Class<? extends CLIOptionHandler> optionHandlerClass;
 public Class<? extends CLIArgumentHandler> argumentHandlerClass;
 public boolean execAndExit;
 public String separator;
 public boolean isMultiValue=false;

}
