import ds.FileSystem;
import exceptions.*;

import java.util.LinkedList;
import java.util.Scanner;

public class main {
    public static void main(String[] args) {
        boolean exit = false;
        FileSystem fileSystem = new FileSystem();
        fileSystem.inputData();
        for (int i = 0; i < 30; i++) {
            fileSystem.create('d', String.valueOf(i));
            fileSystem.pathStack.pop();
        }

        while (!exit) {
            Scanner scan = new Scanner(System.in);
            // Open mode
            if (fileSystem.isUserDataMode()) {
                System.out.println(fileSystem.generatePath());
                boolean openExit = false;
                while (!openExit) {
                    System.out.print(fileSystem.getOpenMode().mode + ": ");
                    String input = scan.next().toLowerCase();
                    switch (input) {
                        case "seek":
                            if (fileSystem.isOutput(fileSystem.getOpenMode().mode)) {
                                System.out.println("Unsupported command: seek");
                            } else {
                                String[] operands = fileSystem.getOperands(2, scan);
                                fileSystem.seek(Integer.parseInt(operands[0]), Integer.parseInt(operands[1]));
                                System.out.println(fileSystem.getOpenMode().pointer);
                            }
                            scan.nextLine(); // Clear scanner
                            break;
                        case "read":
                            if (fileSystem.isOutput(fileSystem.getOpenMode().mode)) {
                                System.out.println("Unsupported command: read");
                            } else {
                                String[] operands = fileSystem.getOperands(1, scan);
                                fileSystem.read(Integer.parseInt(operands[0]));
                                System.out.println(fileSystem.getOpenMode().pointer);
                            }
                            scan.nextLine(); // Clear scanner
                            break;
                        case "write":
                            if (fileSystem.isInput(fileSystem.getOpenMode().mode)) {
                                System.out.println("Unsupported command: write");
                                scan.nextLine(); // Clear scanner
                            } else {
                                int bytes = scan.nextInt();
                                String data = scan.nextLine();
                                fileSystem.write(bytes, data);
                                System.out.println(fileSystem.getOpenMode().pointer);
                            }
                            break;
                        case "close":
                            openExit = fileSystem.close();
                            break;
                        default:
                            System.out.println("Unsupported command1: " + input);
                            scan.nextLine(); // Clear scanner
                    }
                }
            } else {
                System.out.println(fileSystem.generatePath());
                System.out.print("$ ");
                // User input

                String command = scan.next().toLowerCase();
                switch(command) {
                    case "create": {
                        try {
                            String[] operands = fileSystem.getOperands(2, scan);
                            fileSystem.create(operands[0].charAt(0), operands[1]);
                        } catch (UnsupportedOperandException | NoFreeBlockException | NoSuchFileException | UnsupportedTypeFileException e) {
                            System.out.println(e.getMessage());
                        }
                        break;
                    }
                    case "open": {
                        try {
                            String[] operands = fileSystem.getOperands(2, scan);
                            fileSystem.open(operands[0].charAt(0), operands[1]);
                        } catch (NoSuchFileException | UnsupportedModeException e) {
                            System.out.println(e.getMessage());
                        }
                        break;
                    }
                    case "close": {
                        fileSystem.close();
                        break;
                    }
                    case "cd": {
                        try {
                            String[] operands = fileSystem.getOperands(1, scan);
                            LinkedList<String> paths = fileSystem.parseFileName(operands[0]);
                            fileSystem.goToDirectory(paths);
                        } catch (UnsupportedOperandException | NoSuchFileException e) {
                            System.out.println(e.getMessage());
                        }
                        break;
                    }
                    case "ls": {
                        fileSystem.listFiles();
                        break;
                    }
                    case "delete": {
                        String file = scan.next();
                        fileSystem.delete(file);
                        break;
                    }
                    case "exit": {
                        fileSystem.outputData();
                        exit = true;
                        break;
                    }
                    default:
                        System.out.println("Unsupported command: " + command);
                        scan.nextLine(); // Clear scanner
                }
            }
        }
    }
}
