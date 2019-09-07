package Elections.client;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

import java.io.IOException;

class CmdParserUtils {

    static void init(final String[] args, Object o) throws IOException {
        final CmdLineParser parser = new CmdLineParser(o);
        if (args.length < 1) {
            parser.printUsage(System.err);
            System.exit(1);
        }
        try {
            parser.parseArgument(args);
        } catch (CmdLineException e) {
            System.out.println(e.getMessage());
            parser.printUsage(System.err);
            System.exit(1);
        }
    }
}
