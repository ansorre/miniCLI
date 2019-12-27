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
import me.as.lib.core.locale.Localizer;

import java.lang.reflect.Field;
import java.util.Comparator;
import java.util.List;

import static me.as.lib.core.lang.StringExtras.hasChars;
import static me.as.lib.core.lang.StringExtras.isBlank;
import static me.as.lib.core.lang.StringExtras.isNotBlank;


public class HelpHandler implements CLIOptionHandler
{
 Localizer localizer;


 public int handleOption(CommandLineHandler handler, int argsPos, BoxFor2<Field, Settings> option)
 {
  localizer=handler.getLocalizer();

  int t, len;
  List<BoxFor2<Field, Settings>> allOptions=handler.getAllOptions();

  allOptions=ArrayExtras.clone(allOptions);
  allOptions.sort(Comparator.comparingInt(o -> o.element2.helpOrder));

  allOptions.addAll(handler.getAllArguments());


  for (BoxFor2<Field, Settings> b2 : allOptions)
  {
   if (isBlank(b2.element2.name))
    System.out.print("argument");
   else
    System.out.print(b2.element2.name);

   len=ArrayExtras.length(b2.element2.aliases);

   for (t=0;t<len;t++)
   {
    if (b2.element2.aliases[t].charAt(0)!='.')
    {
     System.out.print(" | ");
     System.out.print(b2.element2.aliases[t]);
    }
   }

   String operand=getLocalized(b2.element2.operand);
   if (isNotBlank(operand)) System.out.print(" "+operand);

   System.out.println();

   printUsage(b2);
  }

  return 0;
 }

 private String getLocalized(String source)
 {
  if (isNotBlank(source) && source.startsWith("§"))
   return localizer.getText(source.substring(1));

  return source;
 }


 private void printUsage(BoxFor2<Field, Settings> b2)
 {
  System.out.println(formatLengthAndPadding(getLocalized(b2.element2.usage), 5, 78));
  System.out.println();
 }


 public static String formatLengthAndPadding(String text, int padding, int totalLength)
 {
  StringBuilder sb=new StringBuilder();
  String lines[]=StringExtras.toLines(text);
  int t, len=ArrayExtras.length(lines);

  for (t=0;t<len;t++)
  {
   if (t>0) sb.append("\n");
   _i_formatLengthAndPadding(lines[t], sb, padding, totalLength);
  }

  return sb.toString();
 }


 private static void _i_formatLengthAndPadding(String line, StringBuilder totSb, int padding, int totalLength)
 {
  StringBuilder sb=new StringBuilder();

  while (sb.length()<padding) sb.append(' ');

  if (line.length()+padding>totalLength)
  {
   boolean damnLongWord=false;
   String truncated=(sb.toString()+line).substring(0, totalLength);

   int idx=truncated.lastIndexOf(' ');

   if (idx>0)
   {
    truncated=truncated.substring(0, idx);
    if (!isNotBlank(truncated)) damnLongWord=true;
   }
   else
    damnLongWord=true;

   if (damnLongWord)
   {
    idx=truncated.indexOf(' ', totalLength);
    if (idx>0)
    {
     truncated=truncated.substring(0, idx);
    } else truncated=null;
   }

   if (hasChars(truncated))
   {
    sb.append(truncated.substring(padding)).append("\n");
    String bt=StringExtras.betterTrim(truncated);
    bt=StringExtras.betterTrim(line.substring(line.indexOf(bt)+bt.length()));

    int beterrPadding=truncated.indexOf(" - ");
    beterrPadding=beterrPadding>0 ? beterrPadding+3 : padding;

    _i_formatLengthAndPadding(bt, sb, beterrPadding, totalLength);
   }
   else
    sb.append(line);
  }
  else
   sb.append(line);

  totSb.append(sb);
 }


}


