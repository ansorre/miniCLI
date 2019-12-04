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
import me.as.lib.core.lang.ArrayExtras;
import me.as.lib.core.lang.StringExtras;
import me.as.lib.core.lang.Types;

import java.lang.reflect.Field;

import static me.as.lib.core.lang.ClassExtras.getEnumFromString;
import static me.as.lib.core.lang.ClassExtras.setFieldValue_bruteForce;
import static me.as.lib.core.lang.StringExtras.isNotBlank;
import static me.as.lib.core.lang.StringExtras.replace;


public class BasicCLIOptionHandlers
{

 public static class StringCLIOptionHandler implements CLIOptionHandler
 {
  protected String operand;
  protected String mandatoryMessage;

  public StringCLIOptionHandler()
  {
   this("Missing the value for option '§'");
  }

  public StringCLIOptionHandler(String mandatoryMessage)
  {
   this.mandatoryMessage=mandatoryMessage;
  }


  protected void setFieldValue(CommandLineHandler handler, int argsPos, BoxFor2<Field, Settings> option)
  {
   setFieldValue_bruteForce(handler.getRunnerInstance(), option.element1, operand);
  }

  public int handleOption(CommandLineHandler handler, int argsPos, BoxFor2<Field, Settings> option)
  {
   try
   {
    operand=handler.getArgs()[argsPos+1];
   }
   catch (Throwable tr)
   {
    operand=null;
    if (isNotBlank(mandatoryMessage))
    {
     handler.getProblems().addShowStopper(replace(mandatoryMessage, "§", option.element2.name));
    }
   }

   setFieldValue(handler, argsPos, option);

   return 1;
  }

 }


 public static class BooleanCLIOptionHandlers extends StringCLIOptionHandler
 {
  int returnCount;

  public BooleanCLIOptionHandlers()
  {
   super(null);
  }

  protected void setFieldValue(CommandLineHandler handler, int argsPos, BoxFor2<Field, Settings> option)
  {
   boolean value;

   try
   {
    value=StringExtras.toBoolean(operand);
    returnCount=1;
   }
   catch (Throwable tr)
   {
    value=true;
    returnCount=0;
   }

   setFieldValue_bruteForce(handler.getRunnerInstance(), option.element1, value);
  }


  public int handleOption(CommandLineHandler handler, int argsPos, BoxFor2<Field, Settings> option)
  {
   super.handleOption(handler, argsPos, option);
   return returnCount;
  }
 }

 public static class EnumCLIOptionHandlers extends StringCLIOptionHandler
 {
  protected void setFieldValue(CommandLineHandler handler, int argsPos, BoxFor2<Field, Settings> option)
  {
   Class<Enum> enumClazz=(Class<Enum>)option.element1.getType();

   try
   {
    Enum value=getEnumFromString(enumClazz, operand);

    if (value!=null)
     setFieldValue_bruteForce(handler.getRunnerInstance(), option.element1, value);
    else
     throw new RuntimeException();
   }
   catch (Throwable tr)
   {
    String allValues=StringExtras.mergeEnclosing(ArrayExtras.toArrayOfStrings(enumClazz.getEnumConstants()), ", ", null).substring(2);
    handler.getProblems().addShowStopper("Unknown value '"+operand+"' for option "+option.element2.name+"\nValid values are: "+allValues);
   }
  }
 }

 private static void setPrimitiveFieldValue(CommandLineHandler handler, int argsPos, BoxFor2<Field, Settings> option, String operand, Class primitiveClass)
 {
  Object value=null;

  try
  {
   switch (ArrayExtras.select(Types.classes2, primitiveClass))
   {
    case 3:/* Integer.class */value=StringExtras.toInt(operand);break;
    case 4:/* Long.class    */value=StringExtras.toLong(operand);break;
    case 6:/* Float.class   */value=StringExtras.toFloat(operand);break;
    case 7:/* Double.class  */value=StringExtras.toDouble(operand);break;
    default:throw new RuntimeException();
   }

   setFieldValue_bruteForce(handler.getRunnerInstance(), option.element1, value);
  }
  catch (Throwable tr)
  {
   handler.getProblems().addShowStopper("Missing or invalid value '"+(value!=null ? value : "<null>")+"' for option "+option.element2.name);
  }
 }


 public static class IntegerCLIOptionHandler extends StringCLIOptionHandler
 {
  protected void setFieldValue(CommandLineHandler handler, int argsPos, BoxFor2<Field, Settings> option)
  {
   setPrimitiveFieldValue(handler, argsPos, option, operand, Integer.class);
  }
 }

 public static class LongCLIOptionHandler extends StringCLIOptionHandler
 {
  protected void setFieldValue(CommandLineHandler handler, int argsPos, BoxFor2<Field, Settings> option)
  {
   setPrimitiveFieldValue(handler, argsPos, option, operand, Long.class);
  }
 }

 public static class FloatCLIOptionHandler extends StringCLIOptionHandler
 {
  protected void setFieldValue(CommandLineHandler handler, int argsPos, BoxFor2<Field, Settings> option)
  {
   setPrimitiveFieldValue(handler, argsPos, option, operand, Float.class);
  }
 }

 public static class DoubleCLIOptionHandler extends StringCLIOptionHandler
 {

  protected void setFieldValue(CommandLineHandler handler, int argsPos, BoxFor2<Field, Settings> option)
  {
   setPrimitiveFieldValue(handler, argsPos, option, operand, Double.class);
  }

 }


}
