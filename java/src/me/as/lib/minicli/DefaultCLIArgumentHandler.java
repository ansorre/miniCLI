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


import me.as.lib.core.extra.BoxFor2;
import me.as.lib.core.lang.ClassExtras;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;


public class DefaultCLIArgumentHandler implements CLIArgumentHandler
{

 public void handleArgument(CommandLineHandler handler, int argsPos, BoxFor2<Field, Settings> argument)
 {
  String value=handler.getArgs()[argsPos];
  Object cliInstance=handler.getRunnerInstance();

  Class type=argument.element1.getType();
  if (type==String.class)
   ClassExtras.setFieldValue_bruteForce(cliInstance, argument.element1, value);
  else
  {
   if (ClassExtras.isInstanceOf(type, List.class))
   {
    argument.element2.isMultiValue=true;
    List<String> list=(List<String>)ClassExtras.getFieldValue_bruteForce(cliInstance, argument.element1);

    if (list==null)
     ClassExtras.setFieldValue_bruteForce(cliInstance, argument.element1, list=new ArrayList<>());

    list.add(value);
   }
   else
    throw new RuntimeException("Dunno how to handle type "+type.getName());
  }
 }


}
