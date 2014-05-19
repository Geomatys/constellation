/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 * Copyright 2014 Geomatys.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.constellation.xacml;

/**
 *  URN Constants used in XACML Policy and Request documents.
 *  
 *  @author Anil Saldhana (Redhat)
 *  @author Adrian Custer (Geomatys)
 *  @version 0.3
 *  @since  Jul 6, 2007 (JBoss)
 */
public enum XACMLConstants {

    XACML("xacml"),
    PREFIX_XACML_CONTEXT("xacml-context"),
    UNDERLYING_POLICY("underlying_policy"),
    POLICY_FINDER("policy_finder"),
    POLICY_FINDER_MODULE("policy_finder_module"),
    REQUEST_CTX("request_ctx"),
    RESPONSE_CTX("response_ctx"),
    CONTEXT_SCHEMA("urn:oasis:names:tc:xacml:2.0:context:schema:os"),
    POLICY_SCHEMA("urn:oasis:names:tc:xacml:2.0:policy:schema:os"),
    //
    // ATTRIBUTE IDENTIFIERS
    //
    // subject
    ATTRIBUTEID_SUBJECT_SUBJECTID("urn:oasis:names:tc:xacml:1.0:subject:subject-id"),
    ATTRIBUTEID_SUBJECT_ROLE("urn:oasis:names:tc:xacml:2.0:subject:role"),
    ATTRIBUTEID_SUBJECT_AUTHLOC_DNSNAME("urn:oasis:names:tc:xacml:1.0:subject:authn-locality:dns-name"),
    ATTRIBUTEID_SUBJECT_AUTHLOC_IPADDRESS("urn:oasis:names:tc:xacml:1.0:subject:authn-locality:ip-address"),
    ATTRIBUTEID_SUBJECT_AUTHMETHOD("urn:oasis:names:tc:xacml:1.0:subject:authentication-method"),
    ATTRIBUTEID_SUBJECT_AUTHTIME("urn:oasis:names:tc:xacml:1.0:subject:authentication-time"),
    ATTRIBUTEID_SUBJECT_KEYINFO("urn:oasis:names:tc:xacml:1.0:subject:key-info"),
    ATTRIBUTEID_SUBJECT_REQUESTTIME("urn:oasis:names:tc:xacml:1.0:subject:request-time"),
    ATTRIBUTEID_SUBJECT_NAMEFORMAT("urn:oasis:names:tc:xacml:1.0:subject:name-format"),
    ATTRIBUTEID_SUBJECT_SESSIONSTARTTIME("urn:oasis:names:tc:xacml:1.0:subject:session-start-time"),
    ATTRIBUTEID_SUBJECT_SUBJECTIDQUALIFIER("urn:oasis:names:tc:xacml:1.0:subject:subject-id-qualifier"),
    // subject-category
    ATTRIBUTEID_SUBJECTCAT_ACCESSSUBJECT("urn:oasis:names:tc:xacml:1.0:subject-category:access-subject"),
    ATTRIBUTEID_SUBJECTCAT_CODEBASE("urn:oasis:names:tc:xacml:1.0:subject-category:codebase"),
    ATTRIBUTEID_SUBJECTCAT_INTERMEDIARYSUBJECT("urn:oasis:names:tc:xacml:1.0:subject-category:intermediary-subject"),
    ATTRIBUTEID_SUBJECTCAT_RECIPIENTSUBJECT("urn:oasis:names:tc:xacml:1.0:subject-category:recipient-subject"),
    ATTRIBUTEID_SUBJECTCAT_REQUESTINGMACHINE("urn:oasis:names:tc:xacml:1.0:subject-category:requesting-machine"),
    // resource
    ATTRIBUTEID_RESOURCE_RESOURCEID("urn:oasis:names:tc:xacml:1.0:resource:resource-id"),
    ATTRIBUTEID_RESOURCE_RESOURCETYPE("urn:oasis:names:tc:xacml:1.0:resource:resource-type"),
    ATTRIBUTEID_RESOURCE_TARGETNAMESPACE("urn:oasis:names:tc:xacml:2.0:resource:target-namespace"),
    ATTRIBUTEID_RESOURCE_RESOURCELOC("urn:oasis:names:tc:xacml:1.0:resource:resource-location"),
    ATTRIBUTEID_RESOURCE_XPATH("urn:oasis:names:tc:xacml:1.0:resource:xpath"),
    ATTRIBUTEID_RESOURCE_SIMPLEFILENAME("urn:oasis:names:tc:xacml:1.0:resource:simple-file-name"),
    // action
    ATTRIBUTEID_ACTION_ACTIONID("urn:oasis:names:tc:xacml:1.0:action:action-id"),
    ATTRIBUTEID_ACTION_IMPLIEDACTION("urn:oasis:names:tc:xacml:1.0:action:implied-action"),
    ATTRIBUTEID_ACTION_ACTIONNAMESPACE("urn:oasis:names:tc:xacml:1.0:action:action-namespace"),
    // environment
    ATTRIBUTEID_ENVIRONMENT_CURRENTTIME("urn:oasis:names:tc:xacml:1.0:environment:current-time"),
    ATTRIBUTEID_ENVIRONMENT_CURRENTDATE("urn:oasis:names:tc:xacml:1.0:environment:current-date"),
    ATTRIBUTEID_ENVIRONMENT_CURRENTDATETIME("urn:oasis:names:tc:xacml:1.0:environment:current-dateTime"),
    //
    // FUNCTIONS
    //
    //Equal
    FUNCTION_ANYURI_EQUAL("urn:oasis:names:tc:xacml:1.0:function:anyURI-equal"),
    FUNCTION_BASEBINARY_EQUAL("urn:oasis:names:tc:xacml:1.0:function:base64Binary-equal"),
    FUNCTION_BOOLEAN_EQUAL("urn:oasis:names:tc:xacml:1.0:function:boolean-equal"),
    FUNCTION_DATE_EQUAL("urn:oasis:names:tc:xacml:1.0:function:date-equal"),
    FUNCTION_DATETIME_EQUAL("urn:oasis:names:tc:xacml:1.0:function:dateTime-equal"),
    FUNCTION_DAYTIMEDURATION_EQUAL("urn:oasis:names:tc:xacml:1.0:function:dayTimeDuration-equal"),
    FUNCTION_DOUBLE_EQUAL("urn:oasis:names:tc:xacml:1.0:function:double-equal"),
    FUNCTION_HEXBINARY_EQUAL("urn:oasis:names:tc:xacml:1.0:function:hexBinary-equal"),
    FUNCTION_INTEGER_EQUAL("urn:oasis:names:tc:xacml:1.0:function:integer-equal"),
    FUNCTION_RFC822NAME_EQUAL("urn:oasis:names:tc:xacml:1.0:function:rfc822Name-equal"),
    FUNCTION_STRING_EQUAL("urn:oasis:names:tc:xacml:1.0:function:string-equal"),
    FUNCTION_TIME_EQUAL("urn:oasis:names:tc:xacml:1.0:function:time-equal"),
    FUNCTION_X500NAME_EQUAL("urn:oasis:names:tc:xacml:1.0:function:x500Name-equal"),
    FUNCTION_YEARMONTHDURATION_EQUAL("urn:oasis:names:tc:xacml:1.0:function:yearMonthDuration-equal"),
    //Abs
    FUNCTION_DOUBLE_ABS("urn:oasis:names:tc:xacml:1.0:function:double-abs"),
    FUNCTION_INTEGER_ABS("urn:oasis:names:tc:xacml:1.0:function:integer-abs"),
    //Add
    FUNCTION_DOUBLE_ADD("urn:oasis:names:tc:xacml:1.0:function:double-add"),
    FUNCTION_INTEGER_ADD("urn:oasis:names:tc:xacml:1.0:function:integer-add"),
    //Bag
    FUNCTION_ANYURI_BAG("urn:oasis:names:tc:xacml:1.0:function:anyURI-bag"),
    FUNCTION_ANYURI_BAG_SIZE("urn:oasis:names:tc:xacml:1.0:function:anyURI-bag-size"),
    FUNCTION_ANYURI_IS_IN("urn:oasis:names:tc:xacml:1.0:function:anyURI-is-in"),
    FUNCTION_ANYURI_ONE_AND_ONLY("urn:oasis:names:tc:xacml:1.0:function:anyURI-one-and-only"),
    FUNCTION_BASE64BINARY_BAG("urn:oasis:names:tc:xacml:1.0:function:base64Binary-bag"),
    FUNCTION_BASE64BINARY_BAG_SIZE("urn:oasis:names:tc:xacml:1.0:function:base64Binary-bag-size"),
    FUNCTION_BASE64BINARY_IS_IN("urn:oasis:names:tc:xacml:1.0:function:base64Binary-is-in"),
    FUNCTION_BASE64BINARY_ONE_AND_ONLY("urn:oasis:names:tc:xacml:1.0:function:base64Binary-one-and-only"),
    FUNCTION_BOOLEAN_BAG("urn:oasis:names:tc:xacml:1.0:function:boolean-bag"),
    FUNCTION_BOOLEAN_BAG_SIZE("urn:oasis:names:tc:xacml:1.0:function:boolean-bag-size"),
    FUNCTION_BOOLEAN_IS_IN("urn:oasis:names:tc:xacml:1.0:function:boolean-is-in"),
    FUNCTION_BOOLEAN_ONE_AND_ONLY("urn:oasis:names:tc:xacml:1.0:function:boolean-one-and-only"),
    FUNCTION_DATE_BAG("urn:oasis:names:tc:xacml:1.0:function:date-bag"),
    FUNCTION_DATE_BAG_SIZE("urn:oasis:names:tc:xacml:1.0:function:date-bag-size"),
    FUNCTION_DATE_IS_IN("urn:oasis:names:tc:xacml:1.0:function:date-is-in"),
    FUNCTION_DATE_ONE_AND_ONLY("urn:oasis:names:tc:xacml:1.0:function:date-one-and-only"),
    FUNCTION_DATETIME_BAG("urn:oasis:names:tc:xacml:1.0:function:dateTime-bag"),
    FUNCTION_DATETIME_BAG_SIZE("urn:oasis:names:tc:xacml:1.0:function:dateTime-bag-size"),
    FUNCTION_DATETIME_IS_IN("urn:oasis:names:tc:xacml:1.0:function:dateTime-is-in"),
    FUNCTION_DATETIME_ONE_AND_ONLY("urn:oasis:names:tc:xacml:1.0:function:dateTime-one-and-only"),
    FUNCTION_DAYTIMEDURATION_BAG("urn:oasis:names:tc:xacml:1.0:function:dayTimeDuration-bag"),
    FUNCTION_DAYTIMEDURATION_BAG_SIZE("urn:oasis:names:tc:xacml:1.0:function:dayTimeDuration-bag-size"),
    FUNCTION_DAYTIMEDURATION_IS_IN("urn:oasis:names:tc:xacml:1.0:function:dayTimeDuration-is-in"),
    FUNCTION_DAYTIMEDURATION_ONE_AND_ONLY("urn:oasis:names:tc:xacml:1.0:function:dayTimeDuration-one-and-only"),
    FUNCTION_DOUBLE_BAG("urn:oasis:names:tc:xacml:1.0:function:double-bag"),
    FUNCTION_DOUBLE_BAG_SIZE("urn:oasis:names:tc:xacml:1.0:function:double-bag-size"),
    FUNCTION_DOUBLE_IS_IN("urn:oasis:names:tc:xacml:1.0:function:double-is-in"),
    FUNCTION_DOUBLE_ONE_AND_ONLY("urn:oasis:names:tc:xacml:1.0:function:double-one-and-only"),
    FUNCTION_HEXBINARY_BAG("urn:oasis:names:tc:xacml:1.0:function:hexBinary-bag"),
    FUNCTION_HEXBINARY_BAG_SIZE("urn:oasis:names:tc:xacml:1.0:function:hexBinary-bag-size"),
    FUNCTION_HEXBINARY_IS_IN("urn:oasis:names:tc:xacml:1.0:function:hexBinary-is-in"),
    FUNCTION_HEXBINARY_ONE_AND_ONLY("urn:oasis:names:tc:xacml:1.0:function:hexBinary-one-and-only"),
    FUNCTION_INTEGER_BAG("urn:oasis:names:tc:xacml:1.0:function:integer-bag"),
    FUNCTION_INTEGER_BAG_SIZE("urn:oasis:names:tc:xacml:1.0:function:integer-bag-size"),
    FUNCTION_INTEGER_IS_IN("urn:oasis:names:tc:xacml:1.0:function:integer-is-in"),
    FUNCTION_INTEGER_ONE_AND_ONLY("urn:oasis:names:tc:xacml:1.0:function:integer-one-and-only"),
    FUNCTION_RFC822NAME_BAG("urn:oasis:names:tc:xacml:1.0:function:rfc822Name-bag"),
    FUNCTION_RFC822NAME_BAG_SIZE("urn:oasis:names:tc:xacml:1.0:function:rfc822Name-bag-size"),
    FUNCTION_RFC822NAME_IS_IN("urn:oasis:names:tc:xacml:1.0:function:rfc822Name-is-in"),
    FUNCTION_RFC822NAME_ONE_AND_ONLY("urn:oasis:names:tc:xacml:1.0:function:rfc822Name-one-and-only"),
    FUNCTION_STRING_BAG("urn:oasis:names:tc:xacml:1.0:function:string-bag"),
    FUNCTION_STRING_BAG_SIZE("urn:oasis:names:tc:xacml:1.0:function:string-bag-size"),
    FUNCTION_STRING_IS_IN("urn:oasis:names:tc:xacml:1.0:function:string-is-in"),
    FUNCTION_STRING_ONE_AND_ONLY("urn:oasis:names:tc:xacml:1.0:function:string-one-and-only"),
    FUNCTION_TIME_BAG("urn:oasis:names:tc:xacml:1.0:function:time-bag"),
    FUNCTION_TIME_BAG_SIZE("urn:oasis:names:tc:xacml:1.0:function:time-bag-size"),
    FUNCTION_TIME_IS_IN("urn:oasis:names:tc:xacml:1.0:function:time-is-in"),
    FUNCTION_TIME_ONE_AND_ONLY("urn:oasis:names:tc:xacml:1.0:function:time-one-and-only"),
    FUNCTION_X500NAME_BAG("urn:oasis:names:tc:xacml:1.0:function:x500Name-bag"),
    FUNCTION_X500NAME_BAG_SIZE("urn:oasis:names:tc:xacml:1.0:function:x500Name-bag-size"),
    FUNCTION_X500NAME_IS_IN("urn:oasis:names:tc:xacml:1.0:function:x500Name-is-in"),
    FUNCTION_X500NAME_ONE_AND_ONLY("urn:oasis:names:tc:xacml:1.0:function:x500Name-one-and-only"),
    FUNCTION_YEARMONTHDURATION_BAG("urn:oasis:names:tc:xacml:1.0:function:yearMonthDuration-bag"),
    FUNCTION_YEARMONTHDURATION_BAG_SIZE("urn:oasis:names:tc:xacml:1.0:function:yearMonthDuration-bag-size"),
    FUNCTION_YEARMONTHDURATION_IS_IN("urn:oasis:names:tc:xacml:1.0:function:yearMonthDuration-is-in"),
    FUNCTION_YEARMONTHDURATION_ONE_AND_ONLY("urn:oasis:names:tc:xacml:1.0:function:yearMonthDuration-one-and-only"),
    //Comparison
    FUNCTION_DATE_GREATER_THAN("urn:oasis:names:tc:xacml:1.0:function:date-greater-than"),
    FUNCTION_DATE_GREATER_THAN_OR_EQUAL("urn:oasis:names:tc:xacml:1.0:function:date-greater-than-or-equal"),
    FUNCTION_DATE_LESS_THAN("urn:oasis:names:tc:xacml:1.0:function:date-less-than"),
    FUNCTION_DATE_LESS_THAN_OR_EQUAL("urn:oasis:names:tc:xacml:1.0:function:date-less-than-or-equal"),
    FUNCTION_DATETIME_GREATER_THAN("urn:oasis:names:tc:xacml:1.0:function:dateTime-greater-than"),
    FUNCTION_DATETIME_GREATER_THAN_OR_EQUAL("urn:oasis:names:tc:xacml:1.0:function:dateTime-greater-than-or-equal"),
    FUNCTION_DATETIME_LESS_THAN("urn:oasis:names:tc:xacml:1.0:function:dateTime-less-than"),
    FUNCTION_DATETIME_LESS_THAN_OR_EQUAL("urn:oasis:names:tc:xacml:1.0:function:dateTime-less-than-or-equal"),
    FUNCTION_DOUBLE_GREATER_THAN("urn:oasis:names:tc:xacml:1.0:function:double-greater-than"),
    FUNCTION_DOUBLE_GREATER_THAN_OR_EQUAL("urn:oasis:names:tc:xacml:1.0:function:double-greater-than-or-equal"),
    FUNCTION_DOUBLE_LESS_THAN("urn:oasis:names:tc:xacml:1.0:function:double-less-than"),
    FUNCTION_DOUBLE_LESS_THAN_OR_EQUAL("urn:oasis:names:tc:xacml:1.0:function:double-less-than-or-equal"),
    FUNCTION_INTEGER_GREATER_THAN("urn:oasis:names:tc:xacml:1.0:function:integer-greater-than"),
    FUNCTION_INTEGER_GREATER_THAN_OR_EQUAL("urn:oasis:names:tc:xacml:1.0:function:integer-greater-than-or-equal"),
    FUNCTION_INTEGER_LESS_THAN("urn:oasis:names:tc:xacml:1.0:function:integer-less-than"),
    FUNCTION_INTEGER_LESS_THAN_OR_EQUAL("urn:oasis:names:tc:xacml:1.0:function:integer-less-than-or-equal"),
    FUNCTION_STRING_GREATER_THAN("urn:oasis:names:tc:xacml:1.0:function:string-greater-than"),
    FUNCTION_STRING_GREATER_THAN_OR_EQUAL("urn:oasis:names:tc:xacml:1.0:function:string-greater-than-or-equal"),
    FUNCTION_STRING_LESS_THAN("urn:oasis:names:tc:xacml:1.0:function:string-less-than"),
    FUNCTION_STRING_LESS_THAN_OR_EQUAL("urn:oasis:names:tc:xacml:1.0:function:string-less-than-or-equal"),
    FUNCTION_TIME_IN_RANGE("urn:oasis:names:tc:xacml:2.0:function:time-in-range"),
    FUNCTION_TIME_GREATER_THAN("urn:oasis:names:tc:xacml:1.0:function:time-greater-than"),
    FUNCTION_TIME_GREATER_THAN_OR_EQUAL("urn:oasis:names:tc:xacml:1.0:function:time-greater-than-or-equal"),
    FUNCTION_TIME_LESS_THAN("urn:oasis:names:tc:xacml:1.0:function:time-less-than"),
    FUNCTION_TIME_LESS_THAN_OR_EQUAL("urn:oasis:names:tc:xacml:1.0:function:time-less-than-or-equal"),
    //Date Math
    FUNCTION_DATE_ADD_YEARMONTHDURATION("urn:oasis:names:tc:xacml:1.0:function:date-add-yearMonthDuration"),
    FUNCTION_DATE_SUBTRACT_YEARMONTHDURATION("urn:oasis:names:tc:xacml:1.0:function:date-subtract-yearMonthDuration"),
    FUNCTION_DATETIME_ADD_DAYTIMEDURATION("urn:oasis:names:tc:xacml:1.0:function:dateTime-add-dayTimeDuration"),
    FUNCTION_DATETIME_SUBTRACT_DAYTIMEDURATION("urn:oasis:names:tc:xacml:1.0:function:dateTime-subtract-dayTimeDuration"),
    FUNCTION_DATETIME_ADD_YEARMONTHDURATION("urn:oasis:names:tc:xacml:1.0:function:dateTime-add-yearMonthDuration"),
    FUNCTION_DATETIME_SUBTRACT_YEARMONTHDURATION("urn:oasis:names:tc:xacml:1.0:function:dateTime-subtract-yearMonthDuration"),
    //Divide
    FUNCTION_DOUBLE_DIVIDE("urn:oasis:names:tc:xacml:1.0:function:double-divide"),
    FUNCTION_INTEGER_DIVIDE("urn:oasis:names:tc:xacml:1.0:function:integer-divide"),
    //Floor
    FUNCTION_FLOOR("urn:oasis:names:tc:xacml:1.0:function:floor"),
    //High Order
    FUNCTION_ALL_OF("urn:oasis:names:tc:xacml:1.0:function:all-of"),
    FUNCTION_ALL_OF_ALL("urn:oasis:names:tc:xacml:1.0:function:all-of-all"),
    FUNCTION_ALL_ANY("urn:oasis:names:tc:xacml:1.0:function:all-any"),
    FUNCTION_ANY_OF("urn:oasis:names:tc:xacml:1.0:function:any-of"),
    FUNCTION_ANY_OF_ALL("urn:oasis:names:tc:xacml:1.0:function:any-of-all"),
    FUNCTION_ANY_OF_ANY("urn:oasis:names:tc:xacml:1.0:function:any-of-any"),
    //Logical
    FUNCTION_AND("urn:oasis:names:tc:xacml:1.0:function:and"),
    FUNCTION_OR("urn:oasis:names:tc:xacml:1.0:function:or"),
    FUNCTION_NOT("urn:oasis:names:tc:xacml:1.0:function:not"),
    //Map
    FUNCTION_MAP("urn:oasis:names:tc:xacml:1.0:function:map"),
    //Match
    FUNCTION_REGEXP_URI_MATCH("urn:oasis:names:tc:xacml:1.0:function:regexp-uri-match"),
    FUNCTION_REGEXP_DNSNAME_MATCH("urn:oasis:names:tc:xacml:1.0:function:regexp-dnsName-match"),
    FUNCTION_REGEXP_IPADDRESS_MATCH("urn:oasis:names:tc:xacml:1.0:function:regexp-ipAddress-match"),
    FUNCTION_RFC822NAME_MATCH("urn:oasis:names:tc:xacml:1.0:function:rfc822Name-match"),
    FUNCTION_REGEXP_RFC822NAME_MATCH("urn:oasis:names:tc:xacml:1.0:function:regexp-rfc822Name-match"),
    FUNCTION_REGEXP_STRING_MATCH("urn:oasis:names:tc:xacml:1.0:function:regexp-string-match"),
    FUNCTION_X500NAME_MATCH("urn:oasis:names:tc:xacml:1.0:function:x500Name-match"),
    FUNCTION_REGEXP_X500NAME_MATCH("urn:oasis:names:tc:xacml:1.0:function:regexp-x500Name-match"),
    //Mod
    FUNCTION_INTEGER_MOD("urn:oasis:names:tc:xacml:1.0:function:integer-mod"),
    //Multiply
    FUNCTION_DOUBLE_MULTIPLY("urn:oasis:names:tc:xacml:1.0:function:double-multiply"),
    FUNCTION_INTEGER_MULTIPLY("urn:oasis:names:tc:xacml:1.0:function:integer-multiply"),
    //Nof
    FUNCTION_N_OF("urn:oasis:names:tc:xacml:1.0:function:n-of"),
    //Numeric Convert
    FUNCTION_DOUBLE_TO_INTEGER("urn:oasis:names:tc:xacml:1.0:function:double-to-integer"),
    FUNCTION_INTEGER_TO_DOUBLE("urn:oasis:names:tc:xacml:1.0:function:integer-to-double"),
    //Round
    FUNCTION_ROUND("urn:oasis:names:tc:xacml:1.0:function:round"),
    //Set
    FUNCTION_ANYURI_AT_LEAST_ONE_MEMBER_OF("urn:oasis:names:tc:xacml:1.0:function:anyURI-at-least-one-member-of"),
    FUNCTION_ANYURI_INTERSECTION("urn:oasis:names:tc:xacml:1.0:function:anyURI-intersection"),
    FUNCTION_ANYURI_SET_EQUALS("urn:oasis:names:tc:xacml:1.0:function:anyURI-set-equals"),
    FUNCTION_ANYURI_SUBSET("urn:oasis:names:tc:xacml:1.0:function:anyURI-subset"),
    FUNCTION_ANYURI_UNION("urn:oasis:names:tc:xacml:1.0:function:anyURI-union"),
    FUNCTION_BASE64BINARY_AT_LEAST_ONE_MEMBER_OF("urn:oasis:names:tc:xacml:1.0:function:base64Binary-at-least-one-member-of"),
    FUNCTION_BASE64BINARY_INTERSECTION("urn:oasis:names:tc:xacml:1.0:function:base64Binary-intersection"),
    FUNCTION_BASE64BINARY_SET_EQUALS("urn:oasis:names:tc:xacml:1.0:function:base64Binary-set-equals"),
    FUNCTION_BASE64BINARY_SUBSET("urn:oasis:names:tc:xacml:1.0:function:base64Binary-subset"),
    FUNCTION_BASE64BINARY_UNION("urn:oasis:names:tc:xacml:1.0:function:base64Binary-union"),
    FUNCTION_BOOLEAN_AT_LEAST_ONE_MEMBER_OF("urn:oasis:names:tc:xacml:1.0:function:boolean-at-least-one-member-of"),
    FUNCTION_BOOLEAN_INTERSECTION("urn:oasis:names:tc:xacml:1.0:function:boolean-intersection"),
    FUNCTION_BOOLEAN_SET_EQUALS("urn:oasis:names:tc:xacml:1.0:function:boolean-set-equals"),
    FUNCTION_BOOLEAN_SUBSET("urn:oasis:names:tc:xacml:1.0:function:boolean-subset"),
    FUNCTION_BOOLEAN_UNION("urn:oasis:names:tc:xacml:1.0:function:boolean-union"),
    FUNCTION_DATE_AT_LEAST_ONE_MEMBER_OF("urn:oasis:names:tc:xacml:1.0:function:date-at-least-one-member-of"),
    FUNCTION_DATE_INTERSECTION("urn:oasis:names:tc:xacml:1.0:function:date-intersection"),
    FUNCTION_DATE_SET_EQUALS("urn:oasis:names:tc:xacml:1.0:function:date-set-equals"),
    FUNCTION_DATE_SUBSET("urn:oasis:names:tc:xacml:1.0:function:date-subset"),
    FUNCTION_DATE_UNION("urn:oasis:names:tc:xacml:1.0:function:date-union"),
    FUNCTION_DATETIME_AT_LEAST_ONE_MEMBER_OF("urn:oasis:names:tc:xacml:1.0:function:dateTime-at-least-one-member-of"),
    FUNCTION_DATETIME_INTERSECTION("urn:oasis:names:tc:xacml:1.0:function:dateTime-intersection"),
    FUNCTION_DATETIME_SET_EQUALS("urn:oasis:names:tc:xacml:1.0:function:dateTime-set-equals"),
    FUNCTION_DATETIME_SUBSET("urn:oasis:names:tc:xacml:1.0:function:dateTime-subset"),
    FUNCTION_DATETIME_UNION("urn:oasis:names:tc:xacml:1.0:function:dateTime-union"),
    FUNCTION_DAYTIMEDURATION_AT_LEAST_ONE_MEMBER_OF("urn:oasis:names:tc:xacml:1.0:function:dayTimeDuration-at-least-one-member-of"),
    FUNCTION_DAYTIMEDURATION_INTERSECTION("urn:oasis:names:tc:xacml:1.0:function:dayTimeDuration-intersection"),
    FUNCTION_DAYTIMEDURATION_SET_EQUALS("urn:oasis:names:tc:xacml:1.0:function:dayTimeDuration-set-equals"),
    FUNCTION_DAYTIMEDURATION_SUBSET("urn:oasis:names:tc:xacml:1.0:function:dayTimeDuration-subset"),
    FUNCTION_DAYTIMEDURATION_UNION("urn:oasis:names:tc:xacml:1.0:function:dayTimeDuration-union"),
    FUNCTION_DOUBLE_AT_LEAST_ONE_MEMBER_OF("urn:oasis:names:tc:xacml:1.0:function:double-at-least-one-member-of"),
    FUNCTION_DOUBLE_INTERSECTION("urn:oasis:names:tc:xacml:1.0:function:double-intersection"),
    FUNCTION_DOUBLE_SET_EQUALS("urn:oasis:names:tc:xacml:1.0:function:double-set-equals"),
    FUNCTION_DOUBLE_SUBSET("urn:oasis:names:tc:xacml:1.0:function:double-subset"),
    FUNCTION_DOUBLE_UNION("urn:oasis:names:tc:xacml:1.0:function:double-union"),
    FUNCTION_HEXBINARY_AT_LEAST_ONE_MEMBER_OF("urn:oasis:names:tc:xacml:1.0:function:hexBinary-at-least-one-member-of"),
    FUNCTION_HEXBINARY_INTERSECTION("urn:oasis:names:tc:xacml:1.0:function:hexBinary-intersection"),
    FUNCTION_HEXBINARY_SET_EQUALS("urn:oasis:names:tc:xacml:1.0:function:hexBinary-set-equals"),
    FUNCTION_HEXBINARY_SUBSET("urn:oasis:names:tc:xacml:1.0:function:hexBinary-subset"),
    FUNCTION_HEXBINARY_UNION("urn:oasis:names:tc:xacml:1.0:function:hexBinary-union"),
    FUNCTION_INTEGER_AT_LEAST_ONE_MEMBER_OF("urn:oasis:names:tc:xacml:1.0:function:integer-at-least-one-member-of"),
    FUNCTION_INTEGER_INTERSECTION("urn:oasis:names:tc:xacml:1.0:function:integer-intersection"),
    FUNCTION_INTEGER_SET_EQUALS("urn:oasis:names:tc:xacml:1.0:function:integer-set-equals"),
    FUNCTION_INTEGER_SUBSET("urn:oasis:names:tc:xacml:1.0:function:integer-subset"),
    FUNCTION_INTEGER_UNION("urn:oasis:names:tc:xacml:1.0:function:integer-union"),
    FUNCTION_RFC822NAME_AT_LEAST_ONE_MEMBER_OF("urn:oasis:names:tc:xacml:1.0:function:rfc822Name-at-least-one-member-of"),
    FUNCTION_RFC822NAME_INTERSECTION("urn:oasis:names:tc:xacml:1.0:function:rfc822Name-intersection"),
    FUNCTION_RFC822NAME_SET_EQUALS("urn:oasis:names:tc:xacml:1.0:function:rfc822Name-set-equals"),
    FUNCTION_RFC822NAME_SUBSET("urn:oasis:names:tc:xacml:1.0:function:rfc822Name-subset"),
    FUNCTION_RFC822NAME_UNION("urn:oasis:names:tc:xacml:1.0:function:rfc822Name-union"),
    FUNCTION_STRING_AT_LEAST_ONE_MEMBER_OF("urn:oasis:names:tc:xacml:1.0:function:string-at-least-one-member-of"),
    FUNCTION_STRING_INTERSECTION("urn:oasis:names:tc:xacml:1.0:function:string-intersection"),
    FUNCTION_STRING_SET_EQUALS("urn:oasis:names:tc:xacml:1.0:function:string-set-equals"),
    FUNCTION_STRING_SUBSET("urn:oasis:names:tc:xacml:1.0:function:string-subset"),
    FUNCTION_STRING_UNION("urn:oasis:names:tc:xacml:1.0:function:string-union"),
    FUNCTION_TIME_AT_LEAST_ONE_MEMBER_OF("urn:oasis:names:tc:xacml:1.0:function:time-at-least-one-member-of"),
    FUNCTION_TIME_INTERSECTION("urn:oasis:names:tc:xacml:1.0:function:time-intersection"),
    FUNCTION_TIME_SET_EQUALS("urn:oasis:names:tc:xacml:1.0:function:time-set-equals"),
    FUNCTION_TIME_SUBSET("urn:oasis:names:tc:xacml:1.0:function:time-subset"),
    FUNCTION_TIME_UNION("urn:oasis:names:tc:xacml:1.0:function:time-union"),
    FUNCTION_X500NAME_AT_LEAST_ONE_MEMBER_OF("urn:oasis:names:tc:xacml:1.0:function:x500Name-at-least-one-member-of"),
    FUNCTION_X500NAME_INTERSECTION("urn:oasis:names:tc:xacml:1.0:function:x500Name-intersection"),
    FUNCTION_X500NAME_SET_EQUALS("urn:oasis:names:tc:xacml:1.0:function:x500Name-set-equals"),
    FUNCTION_X500NAME_SUBSET("urn:oasis:names:tc:xacml:1.0:function:x500Name-subset"),
    FUNCTION_X500NAME_UNION("urn:oasis:names:tc:xacml:1.0:function:x500Name-union"),
    FUNCTION_YEARMONTHDURATION_AT_LEAST_ONE_MEMBER_OF("urn:oasis:names:tc:xacml:1.0:function:yearMonthDuration-at-least-one-member-of"),
    FUNCTION_YEARMONTHDURATION_INTERSECTION("urn:oasis:names:tc:xacml:1.0:function:yearMonthDuration-intersection"),
    FUNCTION_YEARMONTHDURATION_SET_EQUALS("urn:oasis:names:tc:xacml:1.0:function:yearMonthDuration-set-equals"),
    FUNCTION_YEARMONTHDURATION_SUBSET("urn:oasis:names:tc:xacml:1.0:function:yearMonthDuration-subset"),
    FUNCTION_YEARMONTHDURATION_UNION("urn:oasis:names:tc:xacml:1.0:function:yearMonthDuration-union"),
    // String Concatenate
    FUNCTION_STRING_CONCATENATE("urn:oasis:names:tc:xacml:2.0:function:string-concatenate"),
    FUNCTION_URL_STRING_CONCATENATE("urn:oasis:names:tc:xacml:2.0:function:url-string-concatenate"),
    //String Normalize
    FUNCTION_STRING_NORMALIZE_SPACE("urn:oasis:names:tc:xacml:1.0:function:string-normalize-space"),
    FUNCTION_STRING_NORMALIZE_TO_LOWER_CASE("urn:oasis:names:tc:xacml:1.0:function:string-normalize-to-lower-case"),
    //Subtract
    FUNCTION_DOUBLE_SUBTRACT("urn:oasis:names:tc:xacml:1.0:function:double-subtract"),
    FUNCTION_INTEGER_SUBTRACT("urn:oasis:names:tc:xacml:1.0:function:integer-subtract"),
    //XPath
    FUNCTION_XPATH_NODE_COUNT("urn:oasis:names:tc:xacml:1.0:function:xpath-node-count"),
    FUNCTION_XPATH_NODE_EQUAL("urn:oasis:names:tc:xacml:1.0:function:xpath-node-equal"),
    FUNCTION_XPATH_NODE_MATCH("urn:oasis:names:tc:xacml:1.0:function:xpath-node-match"),
    //
    //Rule Combining Algorithms
    //
    RULE_COMBINING_FIRST_APPLICABLE("urn:oasis:names:tc:xacml:1.0:rule-combining-algorithm:first-applicable"),
    RULE_COMBINING_DENY_OVERRIDES("urn:oasis:names:tc:xacml:1.0:rule-combining-algorithm:deny-overrides"),
    RULE_COMBINING_ORDERED_DENY_OVERRIDES("urn:oasis:names:tc:xacml:1.0:rule-combining-algorithm:ordered-deny-overrides"),
    RULE_COMBINING_PERMIT_OVERRIDES("urn:oasis:names:tc:xacml:1.0:rule-combining-algorithm:permit-overrides"),
    RULE_COMBINING_ORDERED_PERMIT_OVERRIDES("urn:oasis:names:tc:xacml:1.0:rule-combining-algorithm:ordered-permit-overrides");

    public final String key;

    private XACMLConstants(final String key) {
        this.key = key;
    }

    /**
     * The decision to permit the request
     */
    public static final int DECISION_PERMIT = 0;
    /**
     * The decision to deny the request
     */
    public static final int DECISION_DENY = 1;
    /**
     * The decision that a decision about the request cannot be made
     */
    public static final int DECISION_INDETERMINATE = 2;
    /**
     * The decision that nothing applied to us
     */
    public static final int DECISION_NOT_APPLICABLE = 3;
}
