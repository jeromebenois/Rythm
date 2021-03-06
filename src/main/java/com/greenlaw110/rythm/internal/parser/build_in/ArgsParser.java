package com.greenlaw110.rythm.internal.parser.build_in;

import com.greenlaw110.rythm.internal.CodeBuilder;
import com.greenlaw110.rythm.internal.Keyword;
import com.greenlaw110.rythm.internal.dialect.DialectBase;
import com.greenlaw110.rythm.internal.dialect.Rythm;
import com.greenlaw110.rythm.internal.parser.Directive;
import com.greenlaw110.rythm.internal.parser.ParserBase;
import com.greenlaw110.rythm.spi.IContext;
import com.greenlaw110.rythm.spi.IParser;
import com.greenlaw110.rythm.utils.S;
import com.greenlaw110.rythm.utils.TextBuilder;
import com.stevesoft.pat.Regex;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ArgsParser extends KeywordParserFactory {

    @Override
    public Keyword keyword() {
        return Keyword.ARGS;
    }

    public IParser create(final IContext ctx) {
        return new ParserBase(ctx) {
            /*
             * parse @args {...}
             */
            public TextBuilder go2(String s) {
                Regex r = reg(dialect());
                final List<CodeBuilder.RenderArgDeclaration> ral = new ArrayList<CodeBuilder.RenderArgDeclaration>();
                s = s.replaceAll("[\\n\\r]+", ",");
                while (r.search(s)) {
                    String matched = r.stringMatched();
                    String name = r.stringMatched(4);
                    String type = r.stringMatched(2);
                    String defVal = r.stringMatched(6);
                    ral.add(new CodeBuilder.RenderArgDeclaration(name, type, defVal));
                }
                return new Directive("", ctx()) {
                    @Override
                    public void call() {
                        for (CodeBuilder.RenderArgDeclaration rd: ral) {
                            builder().addRenderArgs(rd);
                        }
                    }
                };
            }
            /*
             * parse @args String s...
             */
            public TextBuilder go() {
                String remain = remain();
                Regex r = new Regex(String.format("%s%s(\\([ \t\f]*\\))?[ \t\f]*((?@{}))", a(), keyword()));
                if (r.search(remain)) {
                    String  s = r.stringMatched(2);
                    s = S.strip(s, "{", "}");
                    step(r.stringMatched().length());
                    return go2(s);
                }
                String key = String.format("%s%s ", a(), keyword());
                if (!remain.startsWith(key)) {
                    raiseParseException("No argument declaration found");
                }
                step(key.length());
                remain = remain();
                r = reg(dialect());
                int step = 0;
                final List<CodeBuilder.RenderArgDeclaration> ral = new ArrayList<CodeBuilder.RenderArgDeclaration>();
                while (r.search(remain)) {
                    String matched = r.stringMatched();
                    if (matched.startsWith("\n") || matched.startsWith("\r")) {
                        break;
                    }
                    step += matched.length();
                    String name = r.stringMatched(4);
                    String type = r.stringMatched(2);
                    String defVal = r.stringMatched(6);
                    ral.add(new CodeBuilder.RenderArgDeclaration(name, type, defVal));
                }
                step(step);
                // strip off the following ";" symbol and line breaks
                char c = peek();
                while (true) {
                    c = peek();
                    if ((' ' == c || ';' == c || '\r' == c || '\n' == c) && ctx.hasRemain()) {
                        step(1);
                    } else {
                        break;
                    }
                }
                return new Directive("", ctx()) {
                    @Override
                    public void call() {
                        for (CodeBuilder.RenderArgDeclaration rd: ral) {
                            builder().addRenderArgs(rd);
                        }
                    }
                };
            }
        };
    }

    public static List<CodeBuilder.RenderArgDeclaration> parseArgDeclaration(String s) {
        final List<CodeBuilder.RenderArgDeclaration> ral = new ArrayList<CodeBuilder.RenderArgDeclaration>();
        Regex r = new ArgsParser().reg((DialectBase)com.greenlaw110.rythm.Rythm.getDialectManager().get());
        while (r.search(s)) {
            String matched = r.stringMatched();
            if (matched.startsWith("\n") || matched.startsWith("\r")) {
                break;
            }
            String name = r.stringMatched(4);
            String type = r.stringMatched(2);
            String defVal = r.stringMatched(5);
            ral.add(new CodeBuilder.RenderArgDeclaration(name, type, defVal));
        }
        return ral;
    }

    public static final String PATTERN = "\\G[ \\t\\x0B\\f]*,?[ \\t\\x0B\\f]*(([\\sa-zA-Z_][\\w$_\\.]*(?@\\<\\>)?(\\[\\])?)[ \\t\\x0B\\f]+([a-zA-Z_][\\w$_]*))([ \\t\\x0B\\f]*=[ \\t\\x0B\\f]*((?@{})|[0-9]|'[.]'|(?@\"\")|[a-zA-Z_][a-zA-Z0-9_\\.]*(?@())*(?@[])*(?@())*(\\.[a-zA-Z][a-zA-Z0-9_\\.]*(?@())*(?@[])*(?@())*)*))?";

    public static final String PATTERN2 = "";

    @Override
    protected String patternStr() {
        return PATTERN;
    }

    protected String patternStr0() {
        return "(%s%s([\\s,]+[a-zA-Z][a-zA-Z0-9_\\.]*(\\<[a-zA-Z][a-zA-Z0-9_\\.,]*\\>)?[\\s]+[a-zA-Z][a-zA-Z0-9_\\.]*)+(;|\\r?\\n)+).*";
    }

    public static void main(String[] args) {
        test1();
    }

    public static void test2() {
        Regex r = new Regex("@args(\\([ \t\f]*\\))?[ \t\f]*((?@{}))");
        String s = "@args() {\nString s = 1}";
        p(s, r);
    }

    public static void test1() {
        String s = ",String,foo,=,\"bar\",int,bar,=,5;,";
        ArgsParser ap = new ArgsParser();
        Regex r = ap.reg(new Rythm());
        //System.out.println(r);
        while (r.search(s)) {
            String m = r.stringMatched();
            if (m.contains("\n") || m.contains("\r")) {
                break;
            } else {
                System.out.println("m: " + (int)m.toCharArray()[0]);
            }
            p(r, 10);
        }
    }

}
