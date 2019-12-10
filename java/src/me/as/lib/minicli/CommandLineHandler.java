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


import jdk.nashorn.api.scripting.ScriptObjectMirror;
import me.as.lib.core.StillUnimplemented;
import me.as.lib.minicli.BasicCLIOptionHandlers.BooleanCLIOptionHandlers;
import me.as.lib.minicli.BasicCLIOptionHandlers.DoubleCLIOptionHandler;
import me.as.lib.minicli.BasicCLIOptionHandlers.EnumCLIOptionHandlers;
import me.as.lib.minicli.BasicCLIOptionHandlers.FloatCLIOptionHandler;
import me.as.lib.minicli.BasicCLIOptionHandlers.IntegerCLIOptionHandler;
import me.as.lib.minicli.BasicCLIOptionHandlers.LongCLIOptionHandler;
import me.as.lib.minicli.BasicCLIOptionHandlers.StringCLIOptionHandler;
import me.as.lib.minicli.Settings.Type;
import me.as.lib.core.extra.BoxFor2;
import me.as.lib.core.lang.ArrayExtras;
import me.as.lib.core.lang.ClassExtras;
import me.as.lib.core.lang.ExceptionExtras;
import me.as.lib.core.locale.Localizer;
import me.as.lib.core.report.Problem;
import me.as.lib.core.report.Problems;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static me.as.lib.core.lang.ClassExtras.isBooleanType;
import static me.as.lib.core.lang.ClassExtras.isDoubleType;
import static me.as.lib.core.lang.ClassExtras.isFloatType;
import static me.as.lib.core.lang.ClassExtras.isInstanceOf;
import static me.as.lib.core.lang.ClassExtras.isIntegerType;
import static me.as.lib.core.lang.ClassExtras.isLongType;
import static me.as.lib.core.lang.ClassExtras.mapAllFields;
import static me.as.lib.core.lang.StringExtras.hasChars;
import static me.as.lib.core.lang.StringExtras.isNotBlank;
import static me.as.lib.core.lang.StringExtras.parseJson;
import static me.as.lib.core.system.FileSystemExtras.isFile;
import static me.as.lib.core.system.FileSystemExtras.loadTextFromFile;


public class CommandLineHandler<R>
{
 public static final String useHelp = "Use --help to learn how to use this program.";

 // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .

 public static <R> R prepare(Class<R> clazz, String args[])
 {
  return prepare(clazz, args, null);
 }

 public static <R> R prepare(Class<R> clazz, String args[], Problems problems)
 {
  return prepare(clazz, args,  problems, null);
 }

 public static <R> R prepare(Class<R> clazz, String args[], Problems problems, Localizer localizer)
 {
  CommandLineHandler<R> clh=new CommandLineHandler<>(clazz, args, problems, localizer);
  clh.createAndConfigureRunner();
  return clh.cliInstance;
 }

 // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .

 private Class<R> clazz;
 private R cliInstance=null;
 private Object realRunnerInstance=null;
 private String args[];
 private Problems problems;
 private Localizer localizer;
 private List<BoxFor2> fieldsAndSettings;
 private HashMap<String, BoxFor2<Field, Settings>> options=new HashMap<>();
 private HashMap<String, BoxFor2<Field, Settings>> byConfigFileKeys=new HashMap<>();
 private ArrayList<BoxFor2<Field, Settings>> allOptions=new ArrayList<>();
 private ArrayList<BoxFor2<Field, Settings>> allArguments=new ArrayList<>();
 private ArrayList<BoxFor2<Field, Settings>> allOfAll=new ArrayList<>();
 private boolean shouldExit=false;
 private boolean configFileWasSpecified=false;
 private int numberOfPassedOptions=0;
 private int numberOfPassedArguments=0;
 private int mandatoryArguments=0;
 private List<Settings> requiredCLIOptions=new ArrayList<>();


 private CommandLineHandler(Class<R> clazz, String args[], Problems problems, Localizer localizer)
 {
  this.clazz=clazz;
  this.args=args;
  this.problems=problems;
  this.localizer=localizer;
 }


 public Localizer getLocalizer()
 {
  return localizer;
 }


 public Object getRunnerInstance()
 {
  if (realRunnerInstance!=null) return realRunnerInstance;
  return cliInstance;
 }

 public List<BoxFor2<Field, Settings>> getAllOptions()
 {
  return allOptions;
 }

 public String[] getArgs()
 {
  return args;
 }

 public Problems getProblems()
 {
  return problems;
 }

 public boolean isConfigFileWasSpecified()
 {
  return configFileWasSpecified;
 }

 public void setConfigFileWasSpecified(boolean configFileWasSpecified)
 {
  this.configFileWasSpecified=configFileWasSpecified;
 }

 public int getNumberOfPassedOptions()
 {
  return numberOfPassedOptions;
 }


 private Annotation getTheRightAnnotation(Annotation annos[])
 {
  return _i_getTheRightAnnotation(annos, 0);
 }

 private Annotation _i_getTheRightAnnotation(Annotation annos[], int deep)
 {
  if (deep>1) return null;

  Annotation res=null;
  int t, len=ArrayExtras.length(annos);

  for (t=0;t<len && res==null;t++)
  {
   if ((annos[t] instanceof CLIOption) || (annos[t] instanceof CLIArgument))
    res=annos[t];
   else
    res=_i_getTheRightAnnotation(annos[t].annotationType().getAnnotations(), deep+1);
  }

  return res;
 }


 private void addOption(BoxFor2<Field, Settings> option)
 {
  String name, all[]=ArrayExtras.append(option.element2.aliases, option.element2.name);
  BoxFor2<Field, Settings> yet;
  int t, len=ArrayExtras.length(all);

  for (t=0;t<len;t++)
  {
   name=all[t];
   if (name.startsWith(".")) name=name.substring(1);

   yet=options.get(name);

   if (yet==null)
   {
    options.put(name, option);
   }
   else
    throw new RuntimeException(
     "The option with name or alias '"+name+"' for field named '"+option.element1.getName()+
     "' overrides the same for field named '"+yet.element1.getName()+"'");
  }
 }


 private CLIOptionHandler getHandler(BoxFor2<Field, Settings> option)
 {
  CLIOptionHandler res;
  Class<? extends CLIOptionHandler> handlerClass;

  if (option.element2.optionHandlerClass!=null && option.element2.optionHandlerClass!=CLIOptionHandler.class)
   handlerClass=option.element2.optionHandlerClass;
  else
  {
   Class theType=option.element1.getType();

   if (isInstanceOf(option.element1.getType(), NoOperand.class))
   {
    if (option.element2.execAndExit) handlerClass=DoNothingAndExit.class;
     else throw new RuntimeException("What to do with this option?");
   }
   else
   {
    if (theType.isEnum())
     handlerClass=EnumCLIOptionHandlers.class;
    else if (isBooleanType(theType))
     handlerClass=BooleanCLIOptionHandlers.class;
    else if (isIntegerType(theType))
     handlerClass=IntegerCLIOptionHandler.class;
    else if (isLongType(theType))
     handlerClass=LongCLIOptionHandler.class;
    else if (isFloatType(theType))
     handlerClass=FloatCLIOptionHandler.class;
    else if (isDoubleType(theType))
     handlerClass=DoubleCLIOptionHandler.class;
    else if (theType==String.class)
     handlerClass=StringCLIOptionHandler.class;
    else handlerClass=null;

   }
  }

  res=ClassExtras.newInstanceByClass(handlerClass);

  return res;
 }


 private void setArgument(BoxFor2<Field, Settings> b2, int argsPos)
 {
  CLIArgumentHandler handler;

  if (b2.element2.argumentHandlerClass==CLIArgumentHandler.class)
   handler=new DefaultCLIArgumentHandler();
  else
   handler=ClassExtras.newInstanceByClass(b2.element2.argumentHandlerClass);

  handler.handleArgument(this, argsPos, b2);
 }


 private void parseUserArgs(boolean fromConfigFile)
 {
  BoxFor2<Field, Settings> option;
  String arg;
  int t, len=ArrayExtras.length(args);

  for (t=0;t<len && !shouldExit;t++)
  {
   arg=args[t];

   option=options.get(arg);

   if (option==null) // then its an arugment
   {
    if (allArguments.size()>0)
    {
     try
     {
      BoxFor2<Field, Settings> b2;

      if (t>=allArguments.size())
      {
       b2=allArguments.get(allArguments.size()-1);
       if (!b2.element2.isMultiValue)
        throw new RuntimeException("Too many values passed for argument");

      }
      else
       b2=allArguments.get(numberOfPassedArguments);

      setArgument(b2, t);

      numberOfPassedArguments++;
     }
     catch (Throwable tr)
     {
      problems.addShowStopper("Invalid arguments or options\n"+useHelp);
     }
    }
    else
    {
     problems.addShowStopper("invalid option '"+arg+"'\n"+useHelp);
     return;
    }
   }
   else
   {
    t+=getHandler(option).handleOption(this, t, option);
    requiredCLIOptions.remove(option.element2);
    numberOfPassedOptions++;

    shouldExit=option.element2.execAndExit || problems.areThereShowStoppers();
   }
  }

  if (!fromConfigFile && numberOfPassedOptions>1 && configFileWasSpecified)
  {
   problems.addShowStopper("No other options can be specified if a config file has been passed");
  }

  if (!shouldExit && (len=requiredCLIOptions.size())>0)
  {
   for (t=0;t<len;t++)
    problems.addShowStopperNoPrefix("Required option '"+requiredCLIOptions.get(t).name+"' has not been specified.");

   problems.addShowStopperNoPrefix(useHelp);
  }
 }



 private void adjustConfigFileName(BoxFor2<Field, Settings> b2)
 {
  if (isNotBlank(b2.element2.configFileName))
  {
   if ("<auto>".equals(b2.element2.configFileName))
   {
    b2.element2.configFileName=b2.element1.getName();
   }
  } else b2.element2.configFileName=null;
 }


 private void createAndConfigureRunner()
 {
  cliInstance=ClassExtras.newInstanceByClass(clazz);
  fieldsAndSettings=new ArrayList<>();

  Field fields[]=ClassExtras.getAllFields(clazz);
  int t, len=ArrayExtras.length(fields);

  for (t=0;t<len;t++)
  {
   Annotation anno=getTheRightAnnotation(fields[t].getAnnotations());

   if (anno!=null)
   {
    Settings settings=new Settings();
    BoxFor2<Field, Settings> b2=new BoxFor2<>(fields[t], settings);
    fieldsAndSettings.add(b2);

    if (anno instanceof CLIOption)
    {
     allOptions.add(b2);
     CLIOption cliOption=(CLIOption)anno;
     settings.type=Type.option;

     settings.name=cliOption.name();
     settings.aliases=cliOption.aliases();
     addOption(b2);

     settings.configFileName=cliOption.configFileName();
     settings.usage=cliOption.usage();
     settings.operand=cliOption.operand();
     settings.documented=cliOption.documented();
     settings.helpOrder=cliOption.helpOrder();
     settings.required=cliOption.required();
     settings.optionHandlerClass=cliOption.handlerClass();
     settings.execAndExit=cliOption.execAndExit();
     settings.separator=cliOption.separator();

     if (settings.required)
      requiredCLIOptions.add(settings);
    }
    else
    {
     CLIArgument cliArgument=(CLIArgument)anno;
     settings.type=Type.argument;

     settings.index=cliArgument.index();
     settings.configFileName=cliArgument.nameInConfigFiles();
     settings.usage=cliArgument.usage();
     settings.operand=cliArgument.operand();
     settings.documented=cliArgument.documented();
     settings.required=cliArgument.required();
     settings.missing=cliArgument.missing();
     settings.argumentHandlerClass=cliArgument.handlerClass();
     settings.execAndExit=cliArgument.execAndExit();
     settings.separator=cliArgument.separator();

     if (settings.required) mandatoryArguments++;

     allArguments.add(b2);
    }

    allOfAll.add(b2);
    adjustConfigFileName(b2);
    byConfigFileKeys.put(b2.element2.configFileName, b2);
   }
  }

  parseUserArgs(false);

  if (shouldExit || problems.areThereShowStoppers())
   cliInstance=null;
  else
  {
   if (numberOfPassedArguments<mandatoryArguments)
   {
    problems.addShowStopper("Missing mandatory arguments!");

    allArguments.forEach(a ->
    {
     if (a.element2.required && isNotBlank(a.element2.missing))
      problems.add(Problem.Type.none, a.element2.missing);
    });

    cliInstance=null;
   }
  }

  if (cliInstance!=null)
   try{ ClassExtras.setFieldValue_bruteForce(cliInstance, "commandLineHandler", this); }catch(Throwable ignore){}
 }


 private void updateAllFields()
 {
  mapAllFields(realRunnerInstance.getClass(),
   map ->
    allOfAll.forEach(
     b2 ->
      b2.element1=map.get(b2.element1.getName())
    )
  );
 }



 @SuppressWarnings("removal")
 public boolean configureByFile(Object realRunner, String configFilePath)
 {
  if (isFile(configFilePath))
  {
   try
   {
    ArrayList<String> largs=new ArrayList<>();
    Map<String, Object> parsed=parseJson(loadTextFromFile(configFilePath));

    for (String key : parsed.keySet())
    {
     BoxFor2<Field, Settings> b2=byConfigFileKeys.get(key);

     if (b2!=null)
     {
      if (b2.element2.type==Type.argument) throw new StillUnimplemented();

      Object oValue=parsed.get(key);

      if (oValue instanceof ScriptObjectMirror)
      {
       final StringBuilder sb=new StringBuilder();
       final ScriptObjectMirror som=(ScriptObjectMirror)oValue;
       String oKeys[]=som.getOwnKeys(false);

       Arrays.asList(oKeys).forEach(
       k ->
       {
        int okInt=Integer.parseInt(k); // otherwise exception rises and that's what I want
        if (sb.length()>0) sb.append(";");
        sb.append(som.get(k).toString());
       });

       oValue=sb.toString();
      }

      String value=oValue!=null ? oValue.toString() : null;

      largs.add(b2.element2.name);
      if (hasChars(value)) largs.add(value);
     }
    }

    if (largs.size()>0)
    {
     args=ArrayExtras.toArrayOfStrings(largs);
     realRunnerInstance=realRunner;
     updateAllFields();
     parseUserArgs(true);
    }
   }
   catch (Throwable tr)
   {
    problems.addShowStopper("Exception while reading the config file '"+configFilePath+"'\n"+ExceptionExtras.getDeepCauseStackTrace(tr));
    return false;
   }

   return true;
  }
  else
  {
   if (numberOfPassedOptions>0)
   {
    problems.addShowStopper("The config file '"+configFilePath+"' is not a file or cannot be read");
   }

   return false;
  }
 }



}
