/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/* Generated By:JJTree&JavaCC: Do not edit this line. ELParserConstants.java */
package org.apache.el.parser;

public interface ELParserConstants {

  int EOF = 0;
  int LITERAL_EXPRESSION = 1;
  int START_DYNAMIC_EXPRESSION = 2;
  int START_DEFERRED_EXPRESSION = 3;
  int INTEGER_LITERAL = 9;
  int FLOATING_POINT_LITERAL = 10;
  int EXPONENT = 11;
  int STRING_LITERAL = 12;
  int BADLY_ESCAPED_STRING_LITERAL = 13;
  int TRUE = 14;
  int FALSE = 15;
  int NULL = 16;
  int END_EXPRESSION = 17;
  int DOT = 18;
  int LPAREN = 19;
  int RPAREN = 20;
  int LBRACK = 21;
  int RBRACK = 22;
  int COLON = 23;
  int COMMA = 24;
  int GT0 = 25;
  int GT1 = 26;
  int LT0 = 27;
  int LT1 = 28;
  int GE0 = 29;
  int GE1 = 30;
  int LE0 = 31;
  int LE1 = 32;
  int EQ0 = 33;
  int EQ1 = 34;
  int NE0 = 35;
  int NE1 = 36;
  int NOT0 = 37;
  int NOT1 = 38;
  int AND0 = 39;
  int AND1 = 40;
  int OR0 = 41;
  int OR1 = 42;
  int EMPTY = 43;
  int INSTANCEOF = 44;
  int MULT = 45;
  int PLUS = 46;
  int MINUS = 47;
  int QUESTIONMARK = 48;
  int DIV0 = 49;
  int DIV1 = 50;
  int MOD0 = 51;
  int MOD1 = 52;
  int IDENTIFIER = 53;
  int FUNCTIONSUFFIX = 54;
  int IMPL_OBJ_START = 55;
  int LETTER = 56;
  int DIGIT = 57;
  int ILLEGAL_CHARACTER = 58;

  int DEFAULT = 0;
  int IN_EXPRESSION = 1;

  String[] tokenImage = {
    "<EOF>",
    "<LITERAL_EXPRESSION>",
    "\"${\"",
    "\"#{\"",
    "\"\\\\\"",
    "\" \"",
    "\"\\t\"",
    "\"\\n\"",
    "\"\\r\"",
    "<INTEGER_LITERAL>",
    "<FLOATING_POINT_LITERAL>",
    "<EXPONENT>",
    "<STRING_LITERAL>",
    "<BADLY_ESCAPED_STRING_LITERAL>",
    "\"true\"",
    "\"false\"",
    "\"null\"",
    "\"}\"",
    "\".\"",
    "\"(\"",
    "\")\"",
    "\"[\"",
    "\"]\"",
    "\":\"",
    "\",\"",
    "\">\"",
    "\"gt\"",
    "\"<\"",
    "\"lt\"",
    "\">=\"",
    "\"ge\"",
    "\"<=\"",
    "\"le\"",
    "\"==\"",
    "\"eq\"",
    "\"!=\"",
    "\"ne\"",
    "\"!\"",
    "\"not\"",
    "\"&&\"",
    "\"and\"",
    "\"||\"",
    "\"or\"",
    "\"empty\"",
    "\"instanceof\"",
    "\"*\"",
    "\"+\"",
    "\"-\"",
    "\"?\"",
    "\"/\"",
    "\"div\"",
    "\"%\"",
    "\"mod\"",
    "<IDENTIFIER>",
    "<FUNCTIONSUFFIX>",
    "\"#\"",
    "<LETTER>",
    "<DIGIT>",
    "<ILLEGAL_CHARACTER>",
  };

}
