package ds;
import exceptions.*;
import java.io.*;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;

public class FileSystem {

    public class OpenMode {
        public char mode; // Mode: input, output, update
        public int pointer; // File pointer
        public int index; // Block of cur data file
        public int parentIndex; // Block of parent dir
        public String fileName;
    }

    Sector[] disk = new Sector[100]; // 100 Blocks
    public LinkedList<Integer> pathStack = new LinkedList<>(); // Store blocks of current path, root is 0
    OpenMode openMode = new OpenMode();
    int size = 1; // Size of used sectors

    public FileSystem() {
        disk[0] = new Directory(0, 0);
        ((Directory) disk[0]).setFree(1);
        pathStack.add(0);
    }

    public String[] getOperands(int num, Scanner scan) {
        String[] operands = new String[num];
        int i = 0;
        while (num > i) {
            if (scan.hasNext()) {
                operands[i++] = scan.next();
            }
        }
        return operands;
    }

    public OpenMode getOpenMode() {
        return openMode;
    }

    public String generatePath() {
        /*
         * Return a String of current path
        */
        StringBuilder sb = new StringBuilder();
        sb.append("root/");
        for (int i = pathStack.size() - 1; i >= 1; i--) {
            Directory curDir = (Directory) disk[pathStack.get(i)];
            SubDir subDir = curDir.getSubDirByIndex(pathStack.get(i - 1));
            while (subDir == null && curDir.forward != 0) {
                curDir = (Directory) disk[curDir.getForward()];
                subDir = curDir.getSubDirByIndex(pathStack.get(i - 1));
            }
            sb.append(subDir.getName());
            sb.append("/");
        }
        return sb.toString();
    }

    public void create(char type, String fileName) {
        /*
         * Create a file with given type and name
         * Side Effects: Update free block index if create a new file.
         * Parameter: full file name(path + target), type(U or D)
         * Return: void
         * Throw: UnsupportedTypeFileException
         */
        if (isDirectory(type)) {
            createDirectory(fileName);
        } else if (isUserData(type)) {
            createUserData(fileName);
        } else {
            throw new UnsupportedTypeFileException("Creating unsupported type of file: " + type);
        }
    }

    private void createUserData(String fileName) {
        /*
         * Create a user data for a full file name, if target is existed, recreate it.
         * Side Effects: Update free block index if create a new file.
         * Parameter: full file name(path + target)
         * Return: void
         */

        String createdFile = getTargetFile(fileName);

        // Target the forward of curDir
        int block = goToForward(createdFile);
        Directory curDir = ((Directory) disk[block]);
        SubDir subDir = curDir.getSubDirByName(createdFile);

        int index;
        if (subDir == null) {
            if (curDir.getFiller() == 31) { // Create a new forward dir
                curDir = (Directory) createForward(curDir, block);
            }
            index = getAndUpdateFreeBlock(); // Update the free block
            curDir.addSubDir(new SubDir('U', createdFile, index, 0));
        } else { // Recreate
            index = subDir.getLink();
            deleteSuccessor(index); // Delete the related user data
            curDir.updateSubDir(createdFile, new SubDir('U', createdFile, index, 0));
        }
        disk[index] = new ds.File(0, 0);
        pathStack.push(index);
        setOpenMode('O', 0, index, block, createdFile);
    }

    private void createDirectory(String fileName) {
        /*
         * Create a directory for a full file name, if target is existed, recreate it.
         * Side Effects: Update free block index if create a new file.
         * Parameter: full file name(path + target)
         * Return: void
         */

        String createdFile = getTargetFile(fileName);

        // Target the forward of curDir
        int block = goToForward(createdFile);
        Directory curDir = ((Directory) disk[block]);
        SubDir subDir = curDir.getSubDirByName(createdFile);

        int index;
        if (subDir == null) {
            if (curDir.getFiller() == 31) { // Create a new forward dir
                curDir = (Directory) createForward(curDir, block);
            }
            index = getAndUpdateFreeBlock(); // Update the free block
            curDir.addSubDir(new SubDir('D', createdFile, index, 0));
        } else { // Recreate
            index = subDir.getLink();
            deleteSuccessor(index); // Delete the related directories
            curDir.updateSubDir(createdFile, new SubDir('D', createdFile, index, 0));
        }
        disk[index] = new Directory(0 , 0);
        pathStack.push(index);
    }

    private String getTargetFile(String path) {
        LinkedList<String> paths = parseFileName(path);
        String createdFile = paths.removeLast();
        goToDirectory(paths);
        return createdFile;
    }

    private int goToForward(String createdFile) {
        int block = pathStack.peek(); // Block of curDir
        Directory curDir = ((Directory) disk[block]);
        SubDir subDir = curDir.getSubDirByName(createdFile);
        while (subDir == null && curDir.forward != 0) {
            block = curDir.forward;
            curDir = ((Directory) disk[block]);
            subDir = curDir.getSubDirByName(createdFile);
        }
        return block;
    }

    private Sector createForward(Sector curSec, int index) {
        int forward = getAndUpdateFreeBlock(); // Update the free block
        curSec.setForward(forward);
        if (curSec instanceof Directory) {
            disk[forward] = new Directory(index , 0);
        } else {
            disk[forward] = new File(index , 0);
        }
        return disk[forward];
    }

    public void delete(String fileName) {
        String deletedFile = getTargetFile(fileName);

        // Target the forward of curDir
        int block = goToForward(deletedFile);
        Directory curDir = ((Directory) disk[block]);
        SubDir target = curDir.deleteSubDirByName(deletedFile);
        if (target == null) {
            throw new NoSuchFileException("No such file exist: " + fileName);
        } else {
            if (curDir.getFiller() == 0 && curDir.getBack() != 0) {
                disk[curDir.getBack()].setForward(0);
                disk[block] = null;
            }
            deleteSuccessor(target.getLink());
        }
    }

    private void deleteSuccessor(int index) {
        /*
         * Delete successor block related to index
         * Side Effect: Decrease corresponding size, set new free block
         */

        if (disk[index].getForward() != 0) {
            deleteSuccessor(disk[index].getForward());
        }
        this.size--;
        disk[index] = null;
        Directory root = (Directory) disk[0];
        root.setFree(index);
    }

    public boolean isUserData(char type) {
        return type == 'U' || type == 'u';
    }

    public boolean isDirectory(char type) {
        return type == 'D' || type == 'd';
    }

    public boolean isInput(char type) {
        return type == 'I' || type == 'i';
    }

    public boolean isOutput(char type) {
        return type == 'O' || type == 'o';
    }

    public boolean isUpdate(char type) {
        return type == 'U' || type == 'u';
    }

    private int getAndUpdateFreeBlock() {
        /*
         * Get and update the free block in disk[0]
         * Side Effect: size++
         * Return: index of free block
         * Throw: NoFreeBlockException if disk is full
         */
        if (isFull()) {
            throw new NoFreeBlockException();
        }
        int free = ((Directory) disk[0]).getFree();
        for (int i = 1; i < disk.length; i++) {
            if (disk[i] == null && i != free) {
                ((Directory) disk[0]).setFree(i);
                break;
            }
        }
        this.size++;
        return free;
    }

    public boolean isFull() {
        return this.size >= disk.length;
    }

    public void goToDirectory(LinkedList<String> files) {
        /*
         * Go to the target directory, and store index of block into pathStack.
         * Parameter: list of fileName(name of file and paths)
         * Return: void
         * Throw: NoSuchFileException
         */

        for (String file : files) {
            if (file.equals("~")) {
                pathStack.clear();
                pathStack.add(0);
            } else if (file.equals("..")) {
                if (pathStack.size() > 1) pathStack.pop();
            } else {
                Directory curDir = (Directory) disk[pathStack.peek()];
                SubDir subDir = curDir.getSubDirByName(file);
                if (subDir == null || isUserData(subDir.getType())) {
                    throw new NoSuchFileException("No such file or directory exists: " + file);
                } else {
                    pathStack.push(subDir.getLink());
                }
            }
        }
    }

    public LinkedList<String> parseFileName(String fileName) {
        /*
         * Parse file name into a string list except ".".
         * For example, ~/abc/sdg. ~, abc, sdg will be put into list.
         * Parameter: String fileName(name of file).
         * Return: list.
         */
        String[] files = fileName.split("/");
        LinkedList<String> list = new LinkedList<>();
        for (String file : files) {
            if (!file.equals(".")) list.add(file);
        }
        return list;
    }

    public void listFiles() {
        Directory curDir = ((Directory) disk[pathStack.peek()]);
        curDir.printSubDirs();
        while (curDir.getForward() != 0) {
            curDir = ((Directory) disk[curDir.getForward()]);
            curDir.printSubDirs();
        }
    }

    public void open(char mode, String fileName) {
        /*
         * Open user data with input, output or update mode
         * Input: only read and seek commands are permitted, pointer at first byte of file
         * Output: only write command is permitted, pointer at last byte of file
         * Update: allows read, seek and write commands are permitted, pointer at first byte of file
         * Parameter: mode('I', 'O', 'U') and name of file
         * Throw: NoSuchFileException and UnsupportedModeException
         */
        String openedFile = getTargetFile(fileName);

        // Target the forward of curDir
        int block = goToForward(openedFile);
        Directory curDir = ((Directory) disk[block]);
        SubDir subDir = curDir.getSubDirByName(openedFile);

        if (subDir == null || isDirectory(subDir.getType()))
            throw new NoSuchFileException("No such file or user data exists: " + openedFile);
        int index = subDir.getLink();
        pathStack.push(index); // update path stack
        if (isInput(mode)) {
            setOpenMode('I', 0, index, block, openedFile);
        } else if (isOutput(mode)) {
            int pointer = subDir.getSize();
            while (disk[index].getForward() != 0) {
                index = disk[index].getForward();
                pointer += 504;
            }
            setOpenMode('O', pointer, subDir.getLink(), block, openedFile);
        } else if (isUpdate(mode)) {
            setOpenMode('U', 0, index, block, openedFile);
        } else {
            throw new UnsupportedModeException("unsupported mode: " + mode);
        }
    }

    private void setOpenMode(char mode, int pointer, int index, int pIndex, String fileName) {
        this.openMode.mode = mode;
        this.openMode.pointer = pointer;
        this.openMode.index = index;
        this.openMode.parentIndex = pIndex;
        this.openMode.fileName = fileName;
    }

    public void write(int bytes, String data) {
        Directory Dir = ((Directory) disk[openMode.parentIndex]);
        SubDir subDir = Dir.getSubDirByName(openMode.fileName);
        ds.File curFile = (ds.File) disk[openMode.index];
        int cur = openMode.pointer;
        while (cur >= curFile.length()) {
            curFile = (ds.File) disk[curFile.getForward()];
            cur -= curFile.length();
        }

        Queue<Character> chars = handleData(data);
        while (bytes > 0) {
            int end = Math.min(bytes + cur, curFile.length());
            bytes = bytes - (end - cur);
            curFile.writeData(cur, end, chars);
            openMode.pointer += end - cur;
            cur = openMode.pointer % curFile.length();
            subDir.setSize(Math.max(end, subDir.getSize()));
            if (bytes > 0) {
                try { // Create new File to store the bytes
                    int free = getAndUpdateFreeBlock();
                    int lastIndex = openMode.index;
                    while (disk[lastIndex].getForward() != 0) lastIndex = disk[lastIndex].getForward();
                    curFile.setForward(free); // Set forward for cur file
                    disk[free] = new ds.File(lastIndex, 0); // Set back for successor
                    curFile = (ds.File) disk[free];
                    subDir.setSize(0);
                } catch (NoFreeBlockException e) {
                    System.out.println("Disk is full, only part of bytes were filled");
                }
            }
        }
    }

    private Queue<Character> handleData(String data) {
        Queue<Character> q = new LinkedList<>();
        int lo = 0;
        for (; lo < data.length(); lo++) {
            if (data.charAt(lo) == '\'') break;
        }
        int hi = data.length() - 1;
        for (; hi >= lo; hi--) {
            if (data.charAt(hi) == '\'') break;
        }
        for (lo = lo + 1; lo < hi; lo++) {
            q.offer(data.charAt(lo));
        }
        return q;
    }

    public void read(int bytes) {
        /*
         * Print n bytes from cur pointer, if not hit the end of file
         * If hit the end, indicate the end of file
         */
        Directory Dir = ((Directory) disk[openMode.parentIndex]);
        SubDir subDir = Dir.getSubDirByName(openMode.fileName);
        ds.File curFile = (ds.File) disk[openMode.index];

        StringBuilder sb = new StringBuilder();
        while (curFile.getForward() != 0) {
            sb.append(curFile.getData(curFile.length()));
            curFile = (ds.File) disk[curFile.getForward()];
        }
        sb.append(curFile.getData(subDir.getSize()));
        int end = Math.min(bytes + openMode.pointer, sb.length());
        String data = sb.substring(openMode.pointer, end);
        System.out.println(data);
        if (bytes + openMode.pointer >= sb.length()) System.out.println("Reach the end of file...");
    }

    public void seek(int base, int offset) {
        /*
         * Set true index of the files(including successor)
         * -1, 0, 1 indicate the beginning, current, end of the file
         */
        Directory Dir = ((Directory) disk[openMode.parentIndex]);
        SubDir subDir = Dir.getSubDirByName(openMode.fileName);
        ds.File curFile = (ds.File) disk[openMode.index];

        int length = subDir.getSize() + countSuccessor(curFile) * curFile.length();
        if (base == -1) openMode.pointer = 0;
        if (base == 1) openMode.pointer = length;
        openMode.pointer += offset;
        openMode.pointer = Math.max(0, openMode.pointer);
        openMode.pointer = Math.min(length, openMode.pointer);
        //openMode.pointer %= length;
        //openMode.pointer += openMode.pointer < 0 ? length : 0;
    }

    private int countSuccessor(ds.File file) {
        int count = 0;
        while (file.getForward() != 0) {
            file = (ds.File) disk[file.getForward()];
            count++;
        }
        return count;
    }

    public boolean close() {
        /*
         * Close cur file or directory, except for the root
         */
        if (pathStack.size() > 1) pathStack.pop();
        return true;
    }

    public boolean isUserDataMode() {
        return disk[pathStack.peek()] instanceof ds.File;
    }

    public void inputData() {
        try {
            BufferedReader reader = new BufferedReader(new FileReader("output.txt"));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(" ");
                if (data[1].equals("D")) {
                    int index = Integer.parseInt(data[0]), back = Integer.parseInt(data[2]), forward = Integer.parseInt(data[3]),
                            free = Integer.parseInt(data[4]), filler = Integer.parseInt(data[5]);
                    disk[index] = new Directory(back, forward, free);
                    int j = 6;
                    for (int i = 0; i < filler; i++) {
                        char type = data[j++].charAt(0);
                        String name = data[j++];
                        int link = Integer.parseInt(data[j++]), size = Integer.parseInt(data[j++]);
                        SubDir subDir = new SubDir(type, name, link, size);
                        ((Directory) disk[index]).addSubDir(subDir);
                    }
                }
                if (data[1].equals("U")) {
                    int index = Integer.parseInt(data[0]), back = Integer.parseInt(data[2]), forward = Integer.parseInt(data[3]);
                    disk[index] = new ds.File(back, forward);
                    ((ds.File) disk[index]).writeData(data);
                }
            }
            reader.close();
        } catch (IOException e) {
            //e.printStackTrace();
            System.out.println("No data file input.");
        }
    }

    public void outputData() {
        /*
         * D: index, D, back, forward, free, filler, subDirs(type, name, link, size)
         * U: index, U, back, forward, user data
         */
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter("output.txt"));
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < disk.length; i++) {
                if (disk[i] != null) {
                    sb.append(i); // index
                    sb.append(' ');
                    if (disk[i] instanceof Directory) {
                        Directory cur = (Directory) disk[i];
                        sb.append(cur.toString());
                    } else {
                        ds.File cur = (File) disk[i];
                        sb.append(cur.toString());
                    }
                    writer.write(sb.toString());
                    writer.newLine();
                    sb.setLength(0);
                }
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
