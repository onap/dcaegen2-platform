/*-
 * ============LICENSE_START=======================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
 * Copyright (C) 2022 Huawei. All rights reserved.
 * ================================================================================
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
 * ============LICENSE_END=========================================================
 */
package org.onap.dcae.genprocessor;

import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ClassFile;
import javassist.bytecode.ConstPool;
import javassist.bytecode.annotation.Annotation;
import javassist.bytecode.annotation.ArrayMemberValue;
import javassist.bytecode.annotation.MemberValue;
import javassist.bytecode.annotation.StringMemberValue;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.text.StringEscapeUtils;
import org.apache.nifi.annotation.documentation.CapabilityDescription;
import org.apache.nifi.annotation.documentation.Tags;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProcessorBuilder {

    static final Logger LOG = LoggerFactory.getLogger(ProcessBuilder.class);

    public static class ProcessorBuilderError extends RuntimeException {
        public ProcessorBuilderError(Throwable e) {
            super("Error while generating DCAEProcessor", e);
        }
    }

    private static Annotation createAnnotationDescription(String description, ConstPool constPool) {
        // https://www.codota.com/code/java/packages/javassist.bytecode showed me that
        // the constructor
        // adds a UTF8 object thing so I'm guessing that the index value when doing
        // addMemberValue
        // should match that of the newly added object otherwise you get a nullpointer
        Annotation annDescrip = new Annotation(CapabilityDescription.class.getName(), constPool);
        // Tried to use the index version of addMemberValue with index of
        // constPool.getSize()-1
        // but didn't work
        annDescrip.addMemberValue("value", new StringMemberValue(description, constPool));
        return annDescrip;
    }

    private static Annotation createAnnotationTags(String[] tags, ConstPool constPool) {
        Annotation annTags = new Annotation(Tags.class.getName(), constPool);
        ArrayMemberValue mv = new ArrayMemberValue(constPool);

        List<MemberValue> elements = new ArrayList<MemberValue>();
        for (String tag : tags) {
            elements.add(new StringMemberValue(tag, constPool));
        }

        mv.setValue(elements.toArray(new MemberValue[elements.size()]));
        // Tried to use the index version of addMemberValue with index of
        // constPool.getSize()-1
        // but didn't work
        annTags.addMemberValue("value", mv);
        return annTags;
    }

    public static String[] createTags(CompSpec compSpec) {
        List<String> tags = new ArrayList<>();
        tags.add("DCAE");

        // TODO: Need to source type from spec
        if (compSpec.name.toLowerCase().contains("collector")) {
            tags.add("collector");
        }

        if (!compSpec.getPublishes().isEmpty()) {
            tags.add("publisher");
        }

        if (!compSpec.getSubscribes().isEmpty()) {
            tags.add("subscriber");
        }

        String[] tagArray = new String[tags.size()];
        return tags.toArray(tagArray);
    }

    public static void addAnnotationsProcessor(CtClass target, String description, String[] tags) {
        ClassFile ccFile = target.getClassFile();
        ConstPool constPool = ccFile.getConstPool();

        AnnotationsAttribute attr = new AnnotationsAttribute(constPool, AnnotationsAttribute.visibleTag);
        attr.addAnnotation(createAnnotationDescription(description, constPool));
        attr.addAnnotation(createAnnotationTags(tags, constPool));

        ccFile.addAttribute(attr);
    }

    public static void addMethod(CtClass target, String methodCode) {
        try {
            CtMethod method = CtMethod.make(methodCode, target);
            target.addMethod(method);
        } catch (CannotCompileException e) {
            LOG.error(String.format("Issue with this code:\n%s", methodCode));
            LOG.error(e.toString(), e);
            throw new ProcessorBuilderError(e);
        }
    }

    private static String createCodeGetter(String methodName, String returnValue) {
        return String.format("public java.lang.String get%s() { return \"%s\"; }", methodName, returnValue);
    }

    public static void setComponentPropertyGetters(CtClass target, Comp comp) {
        addMethod(target, createCodeGetter("Name", comp.compSpec.name));
        addMethod(target, createCodeGetter("Version", comp.compSpec.version));
        addMethod(target, createCodeGetter("ComponentId", comp.id));
        addMethod(target, createCodeGetter("ComponentUrl", comp.selfUrl));
    }

    private static String convertParameterToCode(CompSpec.Parameter param) {
        StringBuilder sb = new StringBuilder("props.add(new org.apache.nifi.components.PropertyDescriptor.Builder()");
        sb.append(String.format(".name(\"%s\")", param.name));
        sb.append(String.format(".displayName(\"%s\")", param.name));
        sb.append(String.format(".description(\"%s\")", StringEscapeUtils.escapeJava(param.description)));
        sb.append(String.format(".defaultValue(\"%s\")", StringEscapeUtils.escapeJava(param.value)));
        sb.append(".build());");
        return sb.toString();
    }

    private static String createCodePropertyDescriptors(CompSpec compSpec) {
        List<String> linesParams = compSpec.parameters.stream().map(p -> convertParameterToCode(p)).collect(Collectors.toList());

        // NOTE: Generics are only partially supported https://www.javassist.org/tutorial/tutorial3.html#generics
        String[] lines = new String[] {"protected java.util.List buildSupportedPropertyDescriptors() {"
            , "java.util.List props = new java.util.LinkedList();"
            , String.join("\n", linesParams.toArray(new String[linesParams.size()]))
            , "return props; }"
        };

        return String.join("\n", lines);
    }

    public static void setProcessorPropertyDescriptors(CtClass target, CompSpec compSpec) {
        addMethod(target, createCodePropertyDescriptors(compSpec));
    }

    private static String createRelationshipName(CompSpec.Connection connection, String direction) {
        // TODO: Revisit this name thing ugh
        return String.format("%s:%s:%s:%s:%s",
            direction, connection.format.toLowerCase(), connection.version, connection.type, connection.configKey);
    }

    private static String convertConnectionToCode(CompSpec.Connection connection, String direction) {
        StringBuilder sb = new StringBuilder("rels.add(new org.apache.nifi.processor.Relationship.Builder()");
        sb.append(String.format(".name(\"%s\")", createRelationshipName(connection, direction)));
        sb.append(".build());");
        return sb.toString();
    }

    private static String createCodeRelationships(CompSpec compSpec) {
        List<String> linesPubs = compSpec.getPublishes().stream().map(c -> convertConnectionToCode(c, "publishes")).collect(Collectors.toList());
        List<String> linesSubs = compSpec.getSubscribes().stream().map(c -> convertConnectionToCode(c, "subscribes")).collect(Collectors.toList());

        String [] lines = new String[] {"protected java.util.Set buildRelationships() {"
            , "java.util.Set rels = new java.util.HashSet();"
            , String.join("\n", linesPubs.toArray(new String[linesPubs.size()]))
            , String.join("\n", linesSubs.toArray(new String[linesSubs.size()]))
            , "return rels; }"
        };

        return String.join("\n", lines);
    }

    public static void setProcessorRelationships(CtClass target, CompSpec compSpec) {
        addMethod(target, createCodeRelationships(compSpec));
    }

}

