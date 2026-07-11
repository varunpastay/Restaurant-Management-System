package com.restro.utility;

import java.io.Console;

/**
 * One-time command-line helper for a deploying integrator to generate a
 * real admin/staff password hash+salt pair, since the seeded rows in
 * sql/seed-data.sql only contain unusable placeholders.
 * <p>
 * Run after building the project, e.g.:
 * {@code java -cp target/classes com.restro.utility.PasswordHashGeneratorTool}
 * (prompts securely, not echoed) or, if no interactive console is available,
 * {@code java -cp target/classes com.restro.utility.PasswordHashGeneratorTool myPassword}
 * (note: this leaves the password visible in shell history / process list -
 * prefer the interactive prompt where possible).
 */
public final class PasswordHashGeneratorTool {

    private PasswordHashGeneratorTool() {
    }

    public static void main(String[] args) {
        String password = readPassword(args);
        if (password == null || password.isBlank()) {
            System.err.println("No password provided. Usage: java -cp <classpath> "
                    + "com.restro.utility.PasswordHashGeneratorTool [password]");
            System.exit(1);
            return;
        }

        String salt = PasswordUtil.generateSalt();
        String hash = PasswordUtil.hash(password, salt);

        System.out.println("password_salt = " + salt);
        System.out.println("password_hash = " + hash);
        System.out.println();
        System.out.println("Example SQL to set the seeded admin's real password:");
        System.out.println("UPDATE admin SET password_hash = '" + hash + "', password_salt = '" + salt
                + "' WHERE username = 'admin';");
    }

    private static String readPassword(String[] args) {
        Console console = System.console();
        if (console != null) {
            char[] chars = console.readPassword("Enter password to hash: ");
            return chars != null ? new String(chars) : null;
        }
        return args.length > 0 ? args[0] : null;
    }
}
