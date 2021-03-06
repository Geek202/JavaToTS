package me.geek.tom.gradletools.generator;

import me.geek.tom.gradletools.utils.api.ApiClass;
import me.geek.tom.gradletools.utils.api.ApiDefinition;
import me.geek.tom.gradletools.utils.api.ApiMethod;

import java.util.*;
import java.util.stream.Collectors;

public class TypeScriptGenerator {

    private static final String CLASS_START = "declare class %s {\n" +
            "\tprivate constructor();";

    private static final String DEFINE_CLASS_METHOD = "public %s(%s): %s;";
    private static final String DEFINE_GLOBAL_METHOD = "declare function %s(%s): %s;";
    private static final String DEFINE_ARGUMENT = "%s: %s";
    private static final String DOC_START = "/**";
    private static final String DOC_END = " */";

    private static final Map<String, String> TYPE_RENAMES = new HashMap<>();
    static {
        TYPE_RENAMES.put("float", "number");
        TYPE_RENAMES.put("int", "number");
        TYPE_RENAMES.put("double", "number");
        TYPE_RENAMES.put("long", "number");
        TYPE_RENAMES.put("String", "string");
    }

    public static List<String> generateDefinition(ApiDefinition api) {
        List<String> lines = new ArrayList<>();

        for (ApiClass cls : api.getClasses()) {
            lines.addAll(generateClass(cls));
            lines.add(""); // Spacing.
        }

        lines.add(""); // Spacing.

        for (ApiMethod method : api.getGlobals()) {
            if (method.getJavaDoc() != null)
                lines.addAll(generateJavadoc(method.getJavaDoc()));
            lines.add(generateGlobalMethod(method));
            lines.add(""); // Spacing.
        }

        return lines;
    }

    private static List<String> generateClass(ApiClass cls) {
        List<String> ret = new ArrayList<>();

        if (cls.getJavaDoc() != null)
            ret.addAll(generateJavadoc(cls.getJavaDoc()));

        ret.add(String.format(CLASS_START, cls.getName()));

        for (ApiMethod method : cls.getMethods()) {
            if (method.getJavaDoc() != null)
                ret.addAll(generateJavadoc(method.getJavaDoc()).stream().map(s->"\t"+s).collect(Collectors.toList()));
            ret.add("\t" + generateClassMethod(method));
        }

        ret.add("}");

        return ret;
    }

    private static String generateGlobalMethod(ApiMethod method) {
        String args = getArgString(method);
        return String.format(DEFINE_GLOBAL_METHOD, method.getName(), args, renameType(method.getReturnType()));
    }

    private static String generateClassMethod(ApiMethod method) {
        String args = getArgString(method);
        return String.format(DEFINE_CLASS_METHOD, method.getName(), args, renameType(method.getReturnType()));
    }

    private static String getArgString(ApiMethod method) {
        return method.getArgs().entrySet().stream().map(entry ->
                String.format(DEFINE_ARGUMENT,
                        entry.getKey(), renameType(entry.getValue())))
                .collect(Collectors.joining(", "));
    }

    private static String renameType(String argumentType) {
        String ret = argumentType;
        for (Map.Entry<String, String> entry : TYPE_RENAMES.entrySet()) {
            ret = ret.replace(entry.getKey(), entry.getValue());
        }
        return ret;
    }

    private static List<String> generateJavadoc(String content) {
        String[] lines = content.split("\n");
        List<String> ret = new ArrayList<>();
        ret.add(DOC_START);
        ret.addAll(Arrays.stream(lines).map(s->" "+s.trim()).collect(Collectors.toList()));
        ret.add(DOC_END);
        return ret.stream().filter(s->s.trim().length()!=0).collect(Collectors.toList());
    }
}
