import java.io.*;
import java.lang.reflect.*;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Stack;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

/**
 * @author TudbuT
 * @since 04 Mar 2022
 */

public class ISBPL {
    // TODO: fully implement JIO
    // public static final boolean ENABLE_JINTEROP = true;
    
    
    static boolean debug = false, printCalls = false;
    public ISBPLDebugger.IPC debuggerIPC = new ISBPLDebugger.IPC();
    ArrayList<ISBPLType> types = new ArrayList<>();
    HashMap<String, ISBPLCallable> level0 = new HashMap<>();
    final ISBPLThreadLocal<Stack<HashMap<String, ISBPLCallable>>> functionStack = ISBPLThreadLocal.withInitial(() -> { Stack<HashMap<String, ISBPLCallable>> stack = new Stack<>(); stack.push(level0); return stack; });
    final ISBPLThreadLocal<Stack<File>> fileStack = ISBPLThreadLocal.withInitial(Stack::new);
    HashMap<ISBPLCallable, Object> varLinks = new HashMap<>();
    HashMap<Object, ISBPLObject> vars = new HashMap<>();
    final ISBPLThreadLocal<ArrayList<String>> lastWords = ISBPLThreadLocal.withInitial(() -> new ArrayList<>(16));
    int exitCode;
    private final ISBPLStreamer streamer = new ISBPLStreamer(this);
    ArrayList<String> included = new ArrayList<>();
    HashMap<String, ISBPLCallable> natives = new HashMap<>();
    
    private ISBPLKeyword getKeyword(String word) {
        switch (word) {
            case "native":
                return (idx, words, file, stack) -> {
                    idx++;
                    addNative(words[idx]);
                    return idx;
                };
            case "func":
                return (idx, words, file, stack) -> createFunction(idx, words, file);
            case "def":
                return (idx, words, file, stack) -> {
                    idx++;
                    Object var = new Object();
                    ISBPLCallable c;
                    vars.put(var, new ISBPLObject(getType("null"), 0));
                    addFunction(words[idx], c = ((stack1) -> stack1.push(vars.get(var))));
                    addFunction("=" + words[idx], (stack1) -> vars.put(var, stack1.pop()));
                    varLinks.put(c, var);
                    return idx;
                };
            case "if":
                return (idx, words, file, stack) -> {
                    idx++;
                    AtomicInteger i = new AtomicInteger(idx);
                    ISBPLCallable callable = readBlock(i, words, file, false);
                    if(stack.pop().isTruthy()) {
                        callable.call(stack);
                    }
                    return i.get();
                };
            case "while":
                return (idx, words, file, stack) -> {
                    idx++;
                    AtomicInteger i = new AtomicInteger(idx);
                    ISBPLCallable cond = readBlock(i, words, file, false);
                    i.getAndIncrement();
                    ISBPLCallable block = readBlock(i, words, file, false);
                    cond.call(stack);
                    while (stack.pop().isTruthy()) {
                        block.call(stack);
                        cond.call(stack);
                    }
                    return i.get();
                };
            case "stop":
                return (idx, words, file, stack) -> {
                    ISBPLObject o = stack.pop();
                    o.checkType(getType("int"));
                    throw new ISBPLStop((int) o.object);
                };
            case "try":
                return (idx, words, file, stack) -> {
                    idx++;
                    ISBPLObject array = stack.pop();
                    array.checkTypeMulti(getType("array"), getType("string"));
                    String[] allowed;
                    if(array.type.name.equals("string")) {
                        allowed = new String[] { toJavaString(array) };
                    }
                    else {
                        ISBPLObject[] arr = ((ISBPLObject[]) array.object);
                        allowed = new String[arr.length];
                        for (int i = 0 ; i < arr.length ; i++) {
                            allowed[i] = toJavaString(arr[i]);
                        }
                    }
                    AtomicInteger i = new AtomicInteger(idx);
                    ISBPLCallable block = readBlock(i, words, file, false);
                    i.getAndIncrement();
                    ISBPLCallable catcher = readBlock(i, words, file, false);
                    try {
                        block.call(stack);
                    } catch (ISBPLError error) {
                        if (Arrays.asList(allowed).contains(error.type) || allowed.length == 1 && allowed[0].equals("all")) {
                            stack.push(toISBPLString(error.message));
                            stack.push(toISBPLString(error.type));
                            catcher.call(stack);
                        }
                        else {
                            throw error;
                        }
                    }
                    return i.get();
                };
            case "do":
                return (idx, words, file, stack) -> {
                    idx++;
                    AtomicInteger i = new AtomicInteger(idx);
                    ISBPLCallable block = readBlock(i, words, file, false);
                    i.getAndIncrement();
                    ISBPLCallable catcher = readBlock(i, words, file, false);
                    try {
                        block.call(stack);
                    } finally {
                        catcher.call(stack);
                    }
                    return i.get();
                };
            case "{":
                return (idx, words, file, stack) -> {
                    AtomicInteger i = new AtomicInteger(idx);
                    ISBPLCallable block = readBlock(i, words, file, false);
                    stack.push(new ISBPLObject(getType("func"), block));
                    return i.get();
                };
            case "fork":
                return (idx, words, file, stack) -> {
                    idx++;
                    AtomicInteger i = new AtomicInteger(idx);
                    Stack<ISBPLObject> s = (Stack<ISBPLObject>) stack.clone();
                    ISBPLCallable block = readBlock(i, words, file, false);
                    long tid = Thread.currentThread().getId();
                    Stack<HashMap<String, ISBPLCallable>> fs = (Stack<HashMap<String, ISBPLCallable>>) functionStack.get().clone();
                    new Thread(() -> {
                        debuggerIPC.run.put(Thread.currentThread().getId(), debuggerIPC.run.getOrDefault(tid, -1) == -1 ? -1 : 0);
                        debuggerIPC.stack.put(Thread.currentThread().getId(), s);
                        functionStack.set(fs);
                        try {
                            block.call(s);
                        } catch (ISBPLStop stop) {
                            if(stop.amount == -1) {
                                System.exit(exitCode);
                            }
                        }
                    }).start();
                    return i.get();
                };
            default:
                return null;
        }
    }
    
    @SuppressWarnings("RedundantCast")
    private void addNative(String name) {
        ISBPLCallable func;
        switch (name) {
            case "alen":
                func = (Stack<ISBPLObject> stack) -> {
                    ISBPLObject o = stack.pop();
                    o.checkType(getType("array"));
                    stack.push(new ISBPLObject(getType("int"), ((ISBPLObject[]) o.object).length));
                };
                break;
            case "aget":
                func = (Stack<ISBPLObject> stack) -> {
                    ISBPLObject i = stack.pop();
                    ISBPLObject o = stack.pop();
                    i.checkType(getType("int"));
                    o.checkType(getType("array"));
                    stack.push(((ISBPLObject[]) o.object)[((int) i.object)]);
                };
                break;
            case "aput":
                func = (Stack<ISBPLObject> stack) -> {
                    ISBPLObject toPut = stack.pop();
                    ISBPLObject i = stack.pop();
                    ISBPLObject o = stack.pop();
                    i.checkType(getType("int"));
                    o.checkType(getType("array"));
                    ((ISBPLObject[]) o.object)[((int) i.object)] = toPut;
                };
                break;
            case "anew":
                func = (Stack<ISBPLObject> stack) -> {
                    ISBPLObject i = stack.pop();
                    i.checkType(getType("int"));
                    ISBPLObject[] arr = new ISBPLObject[((int) i.object)];
                    for (int j = 0 ; j < arr.length ; j++) {
                        arr[j] = new ISBPLObject(getType("null"), 0);
                    }
                    stack.push(new ISBPLObject(getType("array"), arr));
                };
                break;
            case "_array":
                func = (Stack<ISBPLObject> stack) -> {
                    ISBPLObject a = stack.pop();
                    if(a.type.equals(getType("array")))
                        stack.push(a);
                    else if(a.object instanceof ISBPLObject[])
                        stack.push(new ISBPLObject(getType("array"), a.object));
                    else
                        typeError(a.type.name, "array");
                };
                break;
            case "_char":
                func = (Stack<ISBPLObject> stack) -> {
                    ISBPLObject o = stack.pop();
                    o.checkTypeMulti(getType("int"), getType("byte"), getType("char"), getType("float"), getType("long"), getType("double"));
                    stack.push(new ISBPLObject(getType("char"), ((char) o.toLong())));
                };
                break;
            case "_byte":
                func = (Stack<ISBPLObject> stack) -> {
                    ISBPLObject o = stack.pop();
                    o.checkTypeMulti(getType("int"), getType("byte"), getType("char"), getType("float"), getType("long"), getType("double"));
                    stack.push(new ISBPLObject(getType("byte"), ((byte) o.toLong())));
                };
                break;
            case "_int":
                func = (Stack<ISBPLObject> stack) -> {
                    ISBPLObject o = stack.pop();
                    o.checkTypeMulti(getType("int"), getType("byte"), getType("char"), getType("float"), getType("long"), getType("double"));
                    stack.push(new ISBPLObject(getType("int"), ((int) o.toLong())));
                };
                break;
            case "_float":
                func = (Stack<ISBPLObject> stack) -> {
                    ISBPLObject o = stack.pop();
                    o.checkTypeMulti(getType("int"), getType("byte"), getType("char"), getType("float"), getType("long"), getType("double"));
                    stack.push(new ISBPLObject(getType("float"), ((float) o.toDouble())));
                };
                break;
            case "_long":
                func = (Stack<ISBPLObject> stack) -> {
                    ISBPLObject o = stack.pop();
                    o.checkTypeMulti(getType("int"), getType("byte"), getType("char"), getType("float"), getType("long"), getType("double"));
                    stack.push(new ISBPLObject(getType("long"), o.toLong()));
                };
                break;
            case "_double":
                func = (Stack<ISBPLObject> stack) -> {
                    ISBPLObject o = stack.pop();
                    o.checkTypeMulti(getType("int"), getType("byte"), getType("char"), getType("float"), getType("long"), getType("double"));
                    stack.push(new ISBPLObject(getType("double"), o.toDouble()));
                };
                break;
            case "ischar":
                func = (Stack<ISBPLObject> stack) -> {
                    ISBPLObject o = stack.pop();
                    stack.push(new ISBPLObject(getType("int"), o.type.equals(getType("char")) ? 1 : 0));
                };
                break;
            case "isbyte":
                func = (Stack<ISBPLObject> stack) -> {
                    ISBPLObject o = stack.pop();
                    stack.push(new ISBPLObject(getType("int"), o.type.equals(getType("byte")) ? 1 : 0));
                };
                break;
            case "isint":
                func = (Stack<ISBPLObject> stack) -> {
                    ISBPLObject o = stack.pop();
                    stack.push(new ISBPLObject(getType("int"), o.type.equals(getType("int")) ? 1 : 0));
                };
                break;
            case "isfloat":
                func = (Stack<ISBPLObject> stack) -> {
                    ISBPLObject o = stack.pop();
                    stack.push(new ISBPLObject(getType("int"), o.type.equals(getType("float")) ? 1 : 0));
                };
                break;
            case "islong":
                func = (Stack<ISBPLObject> stack) -> {
                    ISBPLObject o = stack.pop();
                    stack.push(new ISBPLObject(getType("int"), o.type.equals(getType("long")) ? 1 : 0));
                };
                break;
            case "isdouble":
                func = (Stack<ISBPLObject> stack) -> {
                    ISBPLObject o = stack.pop();
                    stack.push(new ISBPLObject(getType("int"), o.type.equals(getType("double")) ? 1 : 0));
                };
                break;
            case "isarray":
                func = (Stack<ISBPLObject> stack) -> {
                    ISBPLObject o = stack.pop();
                    stack.push(new ISBPLObject(getType("int"), o.type.equals(getType("array")) ? 1 : 0));
                };
                break;
            case "_layer_call":
                func = (Stack<ISBPLObject> stack) -> {
                    ISBPLObject i = stack.pop();
                    ISBPLObject s = stack.pop();
                    i.checkType(getType("int"));
                    functionStack.get().get(functionStack.get().size() - 1 - ((int) i.object)).get(toJavaString(s)).call(stack);
                };
                break;
            case "reload":
                func = (Stack<ISBPLObject> stack) -> {
                    String filepath = getFilePathForInclude((Stack<ISBPLObject>) stack, fileStack.get());
                    File f = new File(filepath).getAbsoluteFile();
                    try {
                        interpret(f, readFile(f), stack);
                    }
                    catch (IOException e) {
                        throw new ISBPLError("IO", "Couldn't find file " + filepath + " required by reload keyword.");
                    }
                };
                break;
            case "include":
                func = (Stack<ISBPLObject> stack) -> {
                    String filepath = getFilePathForInclude((Stack<ISBPLObject>) stack, fileStack.get());
                    if(!included.contains(filepath)) {
                        File f = new File(filepath).getAbsoluteFile();
                        try {
                            interpret(f, readFile(f), stack);
                        }
                        catch (IOException e) {
                            throw new ISBPLError("IO", "Couldn't find file " + filepath + " required by include keyword.");
                        }
                        included.add(filepath);
                    }
                };
                break;
            case "putchar":
                func = (Stack<ISBPLObject> stack) -> {
                    ISBPLObject c = stack.pop();
                    c.checkType(getType("char"));
                    System.out.print(((char) c.object));
                };
                break;
            case "eputchar":
                func = (Stack<ISBPLObject> stack) -> {
                    ISBPLObject c = stack.pop();
                    c.checkType(getType("char"));
                    System.err.print(((char) c.object));
                };
                break;
            case "_file":
                func = (Stack<ISBPLObject> stack) -> {
                    ISBPLObject s = stack.pop();
                    File f = new File(toJavaString(s));
                    stack.push(new ISBPLObject(getType("file"), f));
                };
                break;
            case "read":
                func = (Stack<ISBPLObject> stack) -> {
                    ISBPLObject end = stack.pop();
                    ISBPLObject begin = stack.pop();
                    ISBPLObject fileToRead = stack.pop();
                    end.checkType(getType("int"));
                    begin.checkType(getType("int"));
                    fileToRead.checkType(getType("file"));
                    try {
                        FileInputStream f = new FileInputStream((File) fileToRead.object);
                        int b = ((int) begin.object);
                        int e = ((int) end.object);
                        byte[] bytes = new byte[e - b];
                        f.read(bytes, b, e);
                        ISBPLObject[] arr = new ISBPLObject[bytes.length];
                        for (int i = 0 ; i < arr.length ; i++) {
                            arr[i] = new ISBPLObject(getType("byte"), bytes[i]);
                        }
                        stack.push(new ISBPLObject(getType("array"), arr));
                    }
                    catch (FileNotFoundException e) {
                        throw new ISBPLError("FileNotFound", "File not found.");
                    }
                    catch (IOException e) {
                        throw new ISBPLError("IO", "File couldn't be read from" + (e.getMessage() != null ? ": " + e.getMessage() : "."));
                    }
                };
                break;
            case "flength":
                func = (Stack<ISBPLObject> stack) -> {
                    ISBPLObject f = stack.pop();
                    f.checkType(getType("file"));
                    stack.push(new ISBPLObject(getType("int"), ((int) ((File) f.object).length())));
                };
                break;
            case "write":
                func = (Stack<ISBPLObject> stack) -> {
                    ISBPLObject content = stack.pop();
                    ISBPLObject fileToWrite = stack.pop();
                    content.checkType(getType("array"));
                    fileToWrite.checkType(getType("file"));
                    throw new ISBPLError("NotImplemented", "_file write is not implemented");
                };
                break;
            case "getos":
                func = (Stack<ISBPLObject> stack) -> {
                    // TODO: This is not done yet, and it's horrible so far.
                    stack.push(toISBPLString("linux"));
                };
                break;
            case "mktype":
                func = (Stack<ISBPLObject> stack) -> {
                    ISBPLObject s = stack.pop();
                    ISBPLType type = registerType(toJavaString(s));
                    stack.push(new ISBPLObject(getType("int"), type.id));
                };
                break;
            case "typename":
                func = (Stack<ISBPLObject> stack) -> {
                    ISBPLObject i = stack.pop();
                    i.checkType(getType("int"));
                    stack.push(toISBPLString(types.get(((int) i.object)).name));
                };
                break;
            case "gettype":
                func = (Stack<ISBPLObject> stack) -> {
                    ISBPLObject o = stack.pop();
                    stack.push(new ISBPLObject(getType("int"), o.type.id));
                };
                break;
            case "settype":
                func = (Stack<ISBPLObject> stack) -> {
                    ISBPLObject i = stack.pop();
                    ISBPLObject o = stack.pop();
                    i.checkType(getType("int"));
                    stack.push(new ISBPLObject(types.get(((int) i.object)), o.object));
                };
                break;
            case "throw":
                func = (Stack<ISBPLObject> stack) -> {
                    ISBPLObject message = stack.pop();
                    ISBPLObject type = stack.pop();
                    String msg = toJavaString(message);
                    String t = toJavaString(type);
                    throw new ISBPLError(t, msg);
                };
                break;
            case "exit":
                func = (Stack<ISBPLObject> stack) -> {
                    ISBPLObject code = stack.pop();
                    code.checkType(getType("int"));
                    exitCode = ((int) code.object);
                    throw new ISBPLStop(0);
                };
                break;
            case "eq":
                func = (Stack<ISBPLObject> stack) -> {
                    ISBPLObject o1 = stack.pop();
                    ISBPLObject o2 = stack.pop();
                    stack.push(new ISBPLObject(getType("int"), o1.equals(o2) ? 1 : 0));
                };
                break;
            case "gt":
                func = (Stack<ISBPLObject> stack) -> {
                    ISBPLObject o2 = stack.pop();
                    ISBPLObject o1 = stack.pop();
                    o1.checkTypeMulti(getType("int"), getType("byte"), getType("char"), getType("float"), getType("long"), getType("double"));
                    o2.checkTypeMulti(getType("int"), getType("byte"), getType("char"), getType("float"), getType("long"), getType("double"));
                    stack.push(new ISBPLObject(getType("int"), o1.toDouble() > o2.toDouble() ? 1 : 0));
                };
                break;
            case "lt":
                func = (Stack<ISBPLObject> stack) -> {
                    ISBPLObject o2 = stack.pop();
                    ISBPLObject o1 = stack.pop();
                    o1.checkTypeMulti(getType("int"), getType("byte"), getType("char"), getType("float"), getType("long"), getType("double"));
                    o2.checkTypeMulti(getType("int"), getType("byte"), getType("char"), getType("float"), getType("long"), getType("double"));
                    stack.push(new ISBPLObject(getType("int"), o1.toDouble() < o2.toDouble() ? 1 : 0));
                };
                break;
            case "not":
                func = (Stack<ISBPLObject> stack) -> stack.push(new ISBPLObject(getType("int"), stack.pop().isTruthy() ? 0 : 1));
                break;
            case "neg":
                func = (Stack<ISBPLObject> stack) -> {
                    ISBPLObject o = stack.pop();
                    o.checkTypeMulti(getType("int"), getType("byte"), getType("char"), getType("float"), getType("long"), getType("double"));
                    stack.push(new ISBPLObject(o.type, o.negative()));
                };
                break;
            case "or":
                func = (Stack<ISBPLObject> stack) -> {
                    ISBPLObject o2 = stack.pop();
                    ISBPLObject o1 = stack.pop();
                    if(o1.isTruthy())
                        stack.push(o1);
                    else
                        stack.push(o2);
                };
                break;
            case "and":
                func = (Stack<ISBPLObject> stack) -> {
                    ISBPLObject o2 = stack.pop();
                    ISBPLObject o1 = stack.pop();
                    // Pushes either 1 or the failed object
                    if (o1.isTruthy()) {
                        if (o2.isTruthy())
                            stack.push(new ISBPLObject(getType("int"), 1));
                        else
                            stack.push(o2);
                    }
                    else
                        stack.push(o1);
                };
                break;
            case "+":
                func = (Stack<ISBPLObject> stack) -> {
                    ISBPLObject o2 = stack.pop();
                    ISBPLObject o1 = stack.pop();
                    o1.checkTypeMulti(getType("int"), getType("byte"), getType("char"), getType("float"), getType("long"), getType("double"));
                    o2.checkTypeMulti(getType("int"), getType("byte"), getType("char"), getType("float"), getType("long"), getType("double"));
                    Object object1 = o1.object;
                    Object object2 = o2.object;
                    ISBPLObject r = null;
                    if(object1 instanceof Integer && object2 instanceof Integer) {
                        r = new ISBPLObject(getType("int"), (int) ((int) (Integer) object1 + (int) (Integer) object2));
                    }
                    if(object1 instanceof Long && object2 instanceof Long) {
                        r = new ISBPLObject(getType("long"), (long) ((long) (Long) object1 + (long) (Long) object2));
                    }
                    if(object1 instanceof Character && object2 instanceof Character) {
                        r = new ISBPLObject(getType("char"), (char) ((char) (Character) object1 + (char) (Character) object2));
                    }
                    if(object1 instanceof Byte && object2 instanceof Byte) {
                        r = new ISBPLObject(getType("byte"), (byte) (Byte.toUnsignedInt((Byte) object1) + Byte.toUnsignedInt((Byte) object2)));
                    }
                    if(object1 instanceof Float && object2 instanceof Float) {
                        r = new ISBPLObject(getType("float"), (float) ((float) (Float) object1 + (float) (Float) object2));
                    }
                    if(object1 instanceof Double && object2 instanceof Double) {
                        r = new ISBPLObject(getType("double"), (double) ((double) (Double) object1 + (double) (Double) object2));
                    }
                    if(r != null)
                        stack.push(r);
                    else
                        typeError(o1.type.name, o2.type.name);
                };
                break;
            case "-":
                func = (Stack<ISBPLObject> stack) -> {
                    ISBPLObject o2 = stack.pop();
                    ISBPLObject o1 = stack.pop();
                    o1.checkTypeMulti(getType("int"), getType("byte"), getType("char"), getType("float"), getType("long"), getType("double"));
                    o2.checkTypeMulti(getType("int"), getType("byte"), getType("char"), getType("float"), getType("long"), getType("double"));
                    Object object1 = o1.object;
                    Object object2 = o2.object;
                    ISBPLObject r = null;
                    if(object1 instanceof Integer && object2 instanceof Integer) {
                        r = new ISBPLObject(getType("int"), (int) ((int) (Integer) object1 - (int) (Integer) object2));
                    }
                    if(object1 instanceof Long && object2 instanceof Long) {
                        r = new ISBPLObject(getType("long"), (long) ((long) (Long) object1 - (long) (Long) object2));
                    }
                    if(object1 instanceof Character && object2 instanceof Character) {
                        r = new ISBPLObject(getType("char"), (char) ((char) (Character) object1 - (char) (Character) object2));
                    }
                    if(object1 instanceof Byte && object2 instanceof Byte) {
                        r = new ISBPLObject(getType("byte"), (byte) (Byte.toUnsignedInt((Byte) object1) - Byte.toUnsignedInt((Byte) object2)));
                    }
                    if(object1 instanceof Float && object2 instanceof Float) {
                        r = new ISBPLObject(getType("float"), (float) ((float) (Float) object1 - (float) (Float) object2));
                    }
                    if(object1 instanceof Double && object2 instanceof Double) {
                        r = new ISBPLObject(getType("double"), (double) ((double) (Double) object1 - (double) (Double) object2));
                    }
                    if(r != null)
                        stack.push(r);
                    else
                        typeError(o1.type.name, o2.type.name);
                };
                break;
            case "/":
                func = (Stack<ISBPLObject> stack) -> {
                    try {
                        ISBPLObject o2 = stack.pop();
                        ISBPLObject o1 = stack.pop();
                        o1.checkTypeMulti(getType("int"), getType("byte"), getType("char"), getType("float"), getType("long"), getType("double"));
                        o2.checkTypeMulti(getType("int"), getType("byte"), getType("char"), getType("float"), getType("long"), getType("double"));
                        Object object1 = o1.object;
                        Object object2 = o2.object;
                        ISBPLObject r = null;
                        if (object1 instanceof Integer && object2 instanceof Integer) {
                            r = new ISBPLObject(getType("int"), (int) ((int) (Integer) object1 / (int) (Integer) object2));
                        }
                        if (object1 instanceof Long && object2 instanceof Long) {
                            r = new ISBPLObject(getType("long"), (long) ((long) (Long) object1 / (long) (Long) object2));
                        }
                        if (object1 instanceof Character && object2 instanceof Character) {
                            r = new ISBPLObject(getType("char"), (char) ((char) (Character) object1 / (char) (Character) object2));
                        }
                        if (object1 instanceof Byte && object2 instanceof Byte) {
                            r = new ISBPLObject(getType("byte"), (byte) (Byte.toUnsignedInt((Byte) object1) / Byte.toUnsignedInt((Byte) object2)));
                        }
                        if (object1 instanceof Float && object2 instanceof Float) {
                            r = new ISBPLObject(getType("float"), (float) ((float) (Float) object1 / (float) (Float) object2));
                        }
                        if (object1 instanceof Double && object2 instanceof Double) {
                            r = new ISBPLObject(getType("double"), (double) ((double) (Double) object1 / (double) (Double) object2));
                        }
                        if (r != null)
                            stack.push(r);
                        else
                            typeError(o1.type.name, o2.type.name);
                    } catch (ArithmeticException ex) {
                        throw new ISBPLError("Arithmetic", "Division by 0");
                    }
                };
                break;
            case "*":
                func = (Stack<ISBPLObject> stack) -> {
                    ISBPLObject o2 = stack.pop();
                    ISBPLObject o1 = stack.pop();
                    o1.checkTypeMulti(getType("int"), getType("byte"), getType("char"), getType("float"), getType("long"), getType("double"));
                    o2.checkTypeMulti(getType("int"), getType("byte"), getType("char"), getType("float"), getType("long"), getType("double"));
                    Object object1 = o1.object;
                    Object object2 = o2.object;
                    ISBPLObject r = null;
                    if(object1 instanceof Integer && object2 instanceof Integer) {
                        r = new ISBPLObject(getType("int"), (int) ((int) (Integer) object1 * (int) (Integer) object2));
                    }
                    if(object1 instanceof Long && object2 instanceof Long) {
                        r = new ISBPLObject(getType("long"), (long) ((long) (Long) object1 * (long) (Long) object2));
                    }
                    if(object1 instanceof Character && object2 instanceof Character) {
                        r = new ISBPLObject(getType("char"), (char) ((char) (Character) object1 * (char) (Character) object2));
                    }
                    if(object1 instanceof Byte && object2 instanceof Byte) {
                        r = new ISBPLObject(getType("byte"), (byte) (Byte.toUnsignedInt((Byte) object1) * Byte.toUnsignedInt((Byte) object2)));
                    }
                    if(object1 instanceof Float && object2 instanceof Float) {
                        r = new ISBPLObject(getType("float"), (float) ((float) (Float) object1 * (float) (Float) object2));
                    }
                    if(object1 instanceof Double && object2 instanceof Double) {
                        r = new ISBPLObject(getType("double"), (double) ((double) (Double) object1 * (double) (Double) object2));
                    }
                    if(r != null)
                        stack.push(r);
                    else
                        typeError(o1.type.name, o2.type.name);
                };
                break;
            case "**":
                func = (Stack<ISBPLObject> stack) -> {
                    ISBPLObject o2 = stack.pop();
                    ISBPLObject o1 = stack.pop();
                    o1.checkTypeMulti(getType("int"), getType("byte"), getType("char"), getType("float"), getType("long"), getType("double"));
                    o2.checkTypeMulti(getType("int"), getType("byte"), getType("char"), getType("float"), getType("long"), getType("double"));
                    Object object1 = o1.object;
                    Object object2 = o2.object;
                    ISBPLObject r = null;
                    if(object1 instanceof Integer && object2 instanceof Integer) {
                        r = new ISBPLObject(getType("int"), (int) Math.pow((int) (Integer) object1, (int) (Integer) object2));
                    }
                    if(object1 instanceof Long && object2 instanceof Long) {
                        r = new ISBPLObject(getType("long"), (long) Math.pow((long) (Long) object1, (long) (Long) object2));
                    }
                    if(object1 instanceof Character && object2 instanceof Character) {
                        r = new ISBPLObject(getType("char"), (char) Math.pow((char) (Character) object1, (char) (Character) object2));
                    }
                    if(object1 instanceof Byte && object2 instanceof Byte) {
                        r = new ISBPLObject(getType("byte"), (byte) Math.pow(Byte.toUnsignedInt((Byte) object1), Byte.toUnsignedInt((Byte) object2)));
                    }
                    if(object1 instanceof Float && object2 instanceof Float) {
                        r = new ISBPLObject(getType("float"), (float) Math.pow((float) (Float) object1, (float) (Float) object2));
                    }
                    if(object1 instanceof Double && object2 instanceof Double) {
                        r = new ISBPLObject(getType("double"), (double) Math.pow((double) (Double) object1, (double) (Double) object2));
                    }
                    if(r != null)
                        stack.push(r);
                    else
                        typeError(o1.type.name, o2.type.name);
                };
                break;
            case "%":
                func = (Stack<ISBPLObject> stack) -> {
                    try {
                        ISBPLObject o2 = stack.pop();
                        ISBPLObject o1 = stack.pop();
                        o1.checkTypeMulti(getType("int"), getType("byte"), getType("char"), getType("float"), getType("long"), getType("double"));
                        o2.checkTypeMulti(getType("int"), getType("byte"), getType("char"), getType("float"), getType("long"), getType("double"));
                        Object object1 = o1.object;
                        Object object2 = o2.object;
                        ISBPLObject r = null;
                        if (object1 instanceof Integer && object2 instanceof Integer) {
                            r = new ISBPLObject(getType("int"), (int) ((int) (Integer) object1 % (int) (Integer) object2));
                        }
                        if (object1 instanceof Long && object2 instanceof Long) {
                            r = new ISBPLObject(getType("long"), (long) ((long) (Long) object1 % (long) (Long) object2));
                        }
                        if (object1 instanceof Character && object2 instanceof Character) {
                            r = new ISBPLObject(getType("char"), (char) ((char) (Character) object1 % (char) (Character) object2));
                        }
                        if (object1 instanceof Byte && object2 instanceof Byte) {
                            r = new ISBPLObject(getType("byte"), (byte) (Byte.toUnsignedInt((Byte) object1) % Byte.toUnsignedInt((Byte) object2)));
                        }
                        if (object1 instanceof Float && object2 instanceof Float) {
                            r = new ISBPLObject(getType("float"), (float) ((float) (Float) object1 % (float) (Float) object2));
                        }
                        if (object1 instanceof Double && object2 instanceof Double) {
                            r = new ISBPLObject(getType("double"), (double) ((double) (Double) object1 % (double) (Double) object2));
                        }
                        if (r != null)
                            stack.push(r);
                        else
                            typeError(o1.type.name, o2.type.name);
                    } catch (ArithmeticException ex) {
                        throw new ISBPLError("Arithmetic", "Division by 0");
                    }
                };
                break;
            case "^":
                func = (Stack<ISBPLObject> stack) -> {
                    ISBPLObject o2 = stack.pop();
                    ISBPLObject o1 = stack.pop();
                    o1.checkTypeMulti(getType("int"), getType("byte"), getType("char"), getType("long"));
                    o2.checkTypeMulti(getType("int"), getType("byte"), getType("char"), getType("long"));
                    Object object1 = o1.object;
                    Object object2 = o2.object;
                    ISBPLObject r = null;
                    if(object1 instanceof Integer && object2 instanceof Integer) {
                        r = new ISBPLObject(getType("int"), (int) ((int) (Integer) object1 ^ (int) (Integer) object2));
                    }
                    if(object1 instanceof Long && object2 instanceof Long) {
                        r = new ISBPLObject(getType("long"), (long) ((long) (Long) object1 ^ (long) (Long) object2));
                    }
                    if(object1 instanceof Character && object2 instanceof Character) {
                        r = new ISBPLObject(getType("char"), (char) ((char) (Character) object1 ^ (char) (Character) object2));
                    }
                    if(object1 instanceof Byte && object2 instanceof Byte) {
                        r = new ISBPLObject(getType("byte"), Byte.toUnsignedInt((Byte) object1) ^ Byte.toUnsignedInt((Byte) object2));
                    }
                    if(r != null)
                        stack.push(r);
                    else
                        typeError(o1.type.name, o2.type.name);
                };
                break;
            case "dup":
                func = (Stack<ISBPLObject> stack) -> {
                    ISBPLObject o = stack.pop();
                    stack.push(new ISBPLObject(o.type, o.object));
                    stack.push(new ISBPLObject(o.type, o.object));
                };
                break;
            case "pop":
                func = Stack::pop;
                break;
            case "swap":
                func = (Stack<ISBPLObject> stack) -> {
                    ISBPLObject o1 = stack.pop();
                    ISBPLObject o2 = stack.pop();
                    stack.push(o1);
                    stack.push(o2);
                };
                break;
            case "_last_word":
                func = (Stack<ISBPLObject> stack) -> {
                    ISBPLObject i = stack.pop();
                    i.checkType(getType("int"));
                    int n = (int) i.object;
                    if(n >= lastWords.get().size())
                        throw new ISBPLError("IllegalArgument", "_last_words called with wrong argument");
                    stack.push(toISBPLString(lastWords.get().get(n)));
                };
                break;
            case "time":
                func = (Stack<ISBPLObject> stack) -> {
                    ISBPLObject i = stack.pop();
                    long n = (long) i.toLong();
                    try {
                        Thread.sleep(n);
                    }
                    catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    stack.push(new ISBPLObject(getType("long"), System.currentTimeMillis()));
                };
                break;
            case "stream":
                func = (Stack<ISBPLObject> stack) -> {
                    ISBPLObject action = stack.pop();
                    action.checkType(getType("int"));
                    int n = ((int) action.object);
                    try {
                        streamer.action(stack, n);
                    }
                    catch (IOException e) {
                        throw new ISBPLError("IO", e.getMessage());
                    }
                };
                break;
            case "_enable_debug":
                func = (Stack<ISBPLObject> stack) -> {
                    if(debuggerIPC.threadID == -1) {
                        ISBPLDebugger debugger = new ISBPLDebugger(this);
                        debugger.start();
                        try {
                            while(debuggerIPC.threadID == -1) Thread.sleep(1);
                        }
                        catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    stack.push(new ISBPLObject(getType("int"), debuggerIPC.port));
                };
                break;
            case "_getvars":
                func = (Stack<ISBPLObject> stack) -> {
                    ISBPLObject[] objects = new ISBPLObject[functionStack.get().size()];
                    int i = 0;
                    for (HashMap<String, ISBPLCallable> map : functionStack.get()) {
                        ArrayList<ISBPLObject> strings = new ArrayList<>();
                        for (String key : map.keySet()) {
                            if(key.startsWith("=")) {
                                strings.add(toISBPLString(key.substring(1)));
                            }
                        }
                        objects[i++] = new ISBPLObject(getType("array"), strings.toArray(new ISBPLObject[0]));
                    }
                    ISBPLObject array = new ISBPLObject(getType("array"), objects);
                    stack.push(array);
                };
                break;
            case "stacksize":
                func = (Stack<ISBPLObject> stack) -> stack.push(new ISBPLObject(getType("int"), stack.size()));
                break;
            case "fcall":
                func = (Stack<ISBPLObject> stack) -> {
                    ISBPLObject o = stack.pop();
                    o.checkType(getType("func"));
                    ((ISBPLCallable) o.object).call(stack);
                };
                break;
            case "subprocess":
                func = (Stack<ISBPLObject> stack) -> {
                    try {
                        Runtime.getRuntime().exec(toJavaString(stack.pop()));
                    } catch(IOException e) {
                        throw new ISBPLError("IO", "Couldn't start process");
                    }
                };
                break;
            case "deffunc":
                func = (stack) -> {
                    ISBPLObject str = stack.pop();
                    ISBPLObject callable = stack.pop();
                    String s = toJavaString(str);
                    callable.checkType(getType("func"));
                    addFunction(s, (ISBPLCallable) callable.object);
                };
                break;
            case "defmethod":
                func = (stack) -> {
                    ISBPLObject type = stack.pop();
                    ISBPLObject callable = stack.pop();
                    ISBPLObject str = stack.pop();
                    type.checkType(getType("int"));
                    callable.checkType(getType("func"));
                    ISBPLType t = types.get((int) type.object);
                    String s = toJavaString(str);
                    addFunction(t, s, (ISBPLCallable) callable.object);
                };
                break;
            case "deffield":
                func = (stack) -> {
                    ISBPLObject type = stack.pop();
                    ISBPLObject str = stack.pop();
                    type.checkType(getType("int"));
                    ISBPLType t = types.get((int) type.object);
                    String s = toJavaString(str);
                    Object var = new Object();
                    addFunction(t, s, (stack1) -> stack1.push(t.varget(stack1.pop()).getOrDefault(var, getNullObject())));
                    addFunction(t, "=" + s, (stack1) -> t.varget(stack1.pop()).put(var, stack1.pop()));
                };
                break;
            case "callmethod":
                func = (stack) -> {
                    ISBPLObject obj = stack.pop();
                    ISBPLObject str = stack.pop();
                    String s = toJavaString(str);
                    stack.push(obj);
                    obj.type.methods.get(s).call(stack);
                };
                break;
            case "jio.class":
                func = (stack) -> {
                    ISBPLObject str = stack.pop();
                    String s = toJavaString(str);
                    try {
                        stack.push(toISBPL(Class.forName(s)));
                    }
                    catch (ClassNotFoundException e) {
                        throw new ISBPLError("JIO", "Class not found");
                    }
                };
                break;
            case "jio.getclass":
                func = (stack) -> {
                    ISBPLObject str = stack.pop();
                    String s = toJavaString(str);
                    try {
                        stack.push(toISBPL((Object) Class.forName(s)));
                    }
                    catch (ClassNotFoundException e) {
                        throw new ISBPLError("JIO", "Class not found");
                    }
                };
                break;
            case "jio.context":
                func = (stack) -> {
                    stack.push(toISBPL(this));
                };
                break;
            case "null":
                func = (stack) -> {
                    stack.push(new ISBPLObject(getType("null"), 0));
                };
                break;
            case "delete":
                func = (stack) -> {
                    ISBPLObject o = stack.pop();
                    o.type.vars.remove(o);
                };
                break;
            default:
                func = natives.get(name);
                break;
        }
        addFunction(name, func);
    }

    ISBPLObject nullObj = null;
    public ISBPLObject getNullObject() {
        if(nullObj == null)
            nullObj = new ISBPLObject(getType("null"), 0);
        return nullObj;
    }
    
    public ISBPLObject toISBPL(Class<?> clazz) {
        ISBPLType type = getType(clazz.getName());
        if(type == null) {
            type = registerType(clazz.getName());
            Class<?> superClass = clazz.getSuperclass();
            if (superClass != null)
                type.superTypes.add(toISBPL(superClass).type);
            Class<?>[] interfaces = clazz.getInterfaces();
            for(Class<?> c : interfaces) {
                type.superTypes.add(toISBPL(c).type);
            }
            if (clazz.isEnum()) {
                for (Object o : clazz.getEnumConstants()) {
                    addFunction(type, o.toString(), stack -> {
                        if(debug)
                            System.err.println("Java GetEnum: " + o);
                        stack.push(toISBPL(o));
                    });
                }
            }
            for (Field field : clazz.getDeclaredFields()) {
                addFunction(type, field.getName(), stack -> {
                    field.setAccessible(true);
                    if(debug)
                        System.err.println("Java Get: " + field);
                    try {
                        stack.push(toISBPL(field.get(stack.pop().object)));
                    }
                    catch (IllegalAccessException ignored) {
                    }
                });
                addFunction(type, "=" + field.getName(), stack -> {
                    field.setAccessible(true);
                    if(debug)
                        System.err.println("Java Set: " + field);
                    try {
                        field.set(stack.pop().object, fromISBPL(stack.pop()));
                    }
                    catch (IllegalAccessException ignored) {
                    }
                });
            }
            for (Method method : clazz.getDeclaredMethods()) {
                addFunction(type, method.getName() + method.getParameterCount(), stack -> {
                    method.setAccessible(true);
                    Object o = stack.pop().object;
                    Class<?>[] parameterTypes = method.getParameterTypes();
                    Object[] params = new Object[method.getParameterCount()];
                    for (int i = params.length - 1 ; i >= 0 ; i--) {
                        params[i] = fromISBPL(stack.pop(), parameterTypes[i]);
                    }
                    if(debug)
                        System.err.println("Java Call: " + method + " - " + Arrays.toString(params));
                    try {
                        Object r = method.invoke(o, params);
                        if(method.getReturnType() != void.class)
                            stack.push(toISBPL(r));
                    }
                    catch (IllegalAccessException ignored) { }
                    catch (InvocationTargetException e) {
                        stack.push(toISBPL(e));
                        throw new ISBPLError("Java", "Java error");
                    }
                });
            }
            for (Constructor<?> constructor : clazz.getDeclaredConstructors()) {
                addFunction(type, "new" + constructor.getParameterCount(), stack -> {
                    constructor.setAccessible(true);
                    stack.pop();
                    Class<?>[] parameterTypes = constructor.getParameterTypes();
                    Object[] params = new Object[constructor.getParameterCount()];
                    for (int i = params.length - 1 ; i >= 0 ; i--) {
                        params[i] = fromISBPL(stack.pop(), parameterTypes[i]);
                    }
                    if(debug)
                        System.err.println("Java Call: new - " + Arrays.toString(params));
                    try {
                        Object r = constructor.newInstance(params);
                        stack.push(toISBPL(r));
                    }
                    catch (IllegalAccessException ignored) { }
                    catch (InvocationTargetException | InstantiationException e) {
                        stack.push(toISBPL(e));
                        throw new ISBPLError("Java", "Java error");
                    }
                });
            }
        }
        return new ISBPLObject(type, null);
    }
    
    private Object fromISBPL(ISBPLObject o) {
        ISBPLType type = o.type;
        if (type.equals(getType("null"))) {
            return null;
        }
        if (type.equals(getType("string")))
            return toJavaString(o);
        if (type.equals(getType("array"))) {
            ISBPLObject[] isbplArray = ((ISBPLObject[]) o.object);
            Object[] array = new Object[isbplArray.length];
            for (int i = 0 ; i < array.length ; i++) {
                array[i] = fromISBPL(isbplArray[i]);
            }
            return array;
        }
        return o.object;
    }

    private Object fromISBPL(ISBPLObject o, Class<?> expectedType) {
        ISBPLType type = o.type;
        if (type.equals(getType("null"))) {
            return null;
        }
        if (type.equals(getType("string")))
            return toJavaString(o);
        if (type.equals(getType("array"))) {
            ISBPLObject[] isbplArray = ((ISBPLObject[]) o.object);
            Object array = new Object[isbplArray.length];
            if(expectedType.isArray())
                array = Array.newInstance(expectedType.getComponentType(), isbplArray.length);
            else
                typeError("array", "matching-array");
            for (int i = 0 ; i < isbplArray.length ; i++) {
                Array.set(array, i, fromISBPL(isbplArray[i]));
            }
            return array;
        }
        return o.object;
    }
    
    public ISBPLObject toISBPL(Object object) {
        if(object == null) {
            return new ISBPLObject(getType("null"), 0);
        }
        ISBPLObject o = toISBPL(object.getClass());
        if (object instanceof String) {
            return toISBPLString(((String) object));
        }
        if (object.getClass().isArray()) {
            ISBPLObject[] isbplArray = new ISBPLObject[Array.getLength(object)];
            for (int i = 0 ; i < isbplArray.length ; i++) {
                isbplArray[i] = toISBPL(Array.get(object, i));
            }
            o.type = getType("array");
            object = isbplArray;
        }
        o.object = object;
        return o;
    }
    
    public void addFunction(ISBPLType type, String name, ISBPLCallable callable) {
        type.methods.put(name, callable);
        type.methods.put("&" + name, stack -> stack.push(new ISBPLObject(getType("func"), callable)));
    }
    
    public void addFunction(String name, ISBPLCallable callable) {
        functionStack.get().peek().put(name, callable);
        functionStack.get().peek().put("&" + name, stack -> stack.push(new ISBPLObject(getType("func"), callable)));
    }
    
    private String getFilePathForInclude(Stack<ISBPLObject> stack, Stack<File> file) {
        ISBPLObject s = stack.pop();
        String filepath = toJavaString(s);
        for (File f : file) {
            filepath = toJavaString(s);
            processPath:
            {
                if (filepath.startsWith("/"))
                    break processPath;
                if (filepath.startsWith("#")) {
                    filepath = System.getenv().getOrDefault("ISBPL_PATH", "/usr/lib/isbpl") + "/" + filepath.substring(1);
                    break processPath;
                }
                filepath = f.getParentFile().getAbsolutePath() + "/" + filepath;
            }
            if(new File(filepath).exists())
                return filepath;
        }
        return filepath;
    }
    
    private int createFunction(int i, String[] words, File file) {
        i++;
        String name = words[i];
        AtomicInteger integer = new AtomicInteger(++i);
        ISBPLCallable callable = readBlock(integer, words, file, true);
        i = integer.get();
        addFunction(name, callable);
        return i;
    }
    
    private ISBPLCallable readBlock(AtomicInteger idx, String[] words, File file, boolean isFunction) {
        ArrayList<String> newWords = new ArrayList<>();
        int i = idx.get();
        i++;
        int lvl = 1;
        for (; i < words.length && lvl > 0 ; i++) {
            String word = words[i];
            if(word.equals("{"))
                lvl++;
            if(word.equals("}")) {
                if(--lvl == 0)
                    break;
            }
            newWords.add(word);
        }
        idx.set(i);
        String[] theWords = newWords.toArray(new String[0]);
        return (stack) -> {
            fileStack.get().push(file);
            interpretRaw(theWords, stack, isFunction);
            fileStack.get().pop();
        };
    }
    
    public String toJavaString(ISBPLObject string) {
        string.checkType(getType("string"));
        ISBPLObject[] array = ((ISBPLObject[]) string.object);
        char[] chars = new char[array.length];
        for (int i = 0 ; i < array.length ; i++) {
            chars[i] = ((char) array[i].object);
        }
        return new String(chars);
    }
    
    public ISBPLObject toISBPLString(String s) {
        char[] chars = s.toCharArray();
        ISBPLObject[] objects = new ISBPLObject[chars.length];
        ISBPLType type = getType("char");
        for (int i = 0 ; i < chars.length ; i++) {
            objects[i] = new ISBPLObject(type, chars[i]);
        }
        return new ISBPLObject(getType("string"), objects);
    }
    
    public ISBPLType registerType(String name) {
        ISBPLType type = new ISBPLType(name, types.size());
        types.add(type);
        return type;
    }
    
    // These will die as soon as std creates the real types and any types created before these are replaced become invalid.
    static final ISBPLType defaultTypeInt = new ISBPLType("int", -2);
    static final ISBPLType defaultTypeString = new ISBPLType("string", -1);
    
    public ISBPLType getType(String name) {
        for (int i = 0 ; i < types.size() ; i++) {
            if(types.get(i).name.equals(name))
                return types.get(i);
        }
        if(name.equals("int"))
            return defaultTypeInt;
        if(name.equals("string"))
            return defaultTypeString;
        return null;
    }
    public void typeError(String got, String wanted) {
        throw new ISBPLError("IncompatibleTypes", "Incompatible types: " + got + " - " + wanted);
    }
    
    public void interpret(File file, String code, Stack<ISBPLObject> stack) {
        fileStack.get().push(file);
        code = cleanCode(code);
        String[] words = splitWords(code);
        interpretRaw(words, stack, false);
        fileStack.get().pop();
    }
    
    private void interpretRaw(String[] words, Stack<ISBPLObject> stack, boolean isFunction) {
        if(isFunction)
            functionStack.get().push(new HashMap<>());
        try {
            nextWord: for (int i = 0 ; i < words.length ; i++) {
                String word = words[i];
                if (word.length() == 0)
                    continue;
                if(printCalls) {
                    StringBuilder s = new StringBuilder();
                    for (int x = 0 ; x < functionStack.get().size() ; x++) {
                        s.append("\t");
                    }
                    System.err.println(s + word + "\t\t" + (debug ? stack : ""));
                }
                while (debuggerIPC.run.getOrDefault(Thread.currentThread().getId(), -1) == 0) Thread.sleep(1);
                int rid = debuggerIPC.run.getOrDefault(Thread.currentThread().getId(), -1);
                if(rid < 0) {
                    if(rid < -1) {
                        if (rid == -2) {
                            if (word.equals(debuggerIPC.until)) {
                                debuggerIPC.run.put(Thread.currentThread().getId(), 0);
                                while (debuggerIPC.run.get(Thread.currentThread().getId()) != -1) Thread.sleep(1);
                            }
                        }
                        if (rid == -3 && Thread.currentThread().getId() != debuggerIPC.threadID) {
                            while (debuggerIPC.run.get(Thread.currentThread().getId()) == -3) Thread.sleep(1);
                        }
                    }
                } else
                    debuggerIPC.run.put(Thread.currentThread().getId(), debuggerIPC.run.get(Thread.currentThread().getId()) - 1);
                lastWords.get().add(0, word);
                while(lastWords.get().size() > 16)
                    lastWords.get().remove(lastWords.get().size() - 1);
                ISBPLKeyword keyword = getKeyword(word);
                if (keyword != null) {
                    i = keyword.call(i, words, fileStack.get().peek(), stack);
                    continue;
                }
                if(stack.size() > 0) {
                    // Doing this nonrecursively because it's much better despite the slight readability disadvantage.
                    ISBPLType type = stack.peek().type;
                    Queue<ISBPLType> types = new LinkedList<>();
                    types.add(type);
                    while (!types.isEmpty()) {
                        type = types.poll();
                        types.addAll(type.superTypes);
                        if(type.methods.containsKey(word)) {
                            type.methods.get(word).call(stack);
                            continue nextWord;
                        }
                    }
                }
                ISBPLCallable func = functionStack.get().peek().get(word);
                if(func != null) {
                    func.call(stack);
                    continue;
                }
                func = functionStack.get().get(0).get(word);
                if(func != null) {
                    func.call(stack);
                    continue;
                }
                if (word.startsWith("\"")) {
                    stack.push(toISBPLString(word.substring(1)));
                    continue;
                }
                try {
                    stack.push(new ISBPLObject(getType("int"), Integer.parseInt(word)));
                    continue;
                } catch (Exception ignore) {}
                try {
                    stack.push(new ISBPLObject(getType("long"), Long.parseLong(word)));
                    continue;
                } catch (Exception ignore) {}
                try {
                    stack.push(new ISBPLObject(getType("float"), Float.parseFloat(word)));
                    continue;
                } catch (Exception ignore) {}
                try {
                    stack.push(new ISBPLObject(getType("double"), Double.parseDouble(word)));
                    continue;
                } catch (Exception ignore) {}
                throw new ISBPLError("InvalidWord", word + " is not a function, object, or keyword.");
            }
        } catch (ISBPLStop stop) {
            if(stop.amount == 0)
                return;
            throw new ISBPLStop(stop.amount);
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
        finally {
            if(isFunction) {
                HashMap<String, ISBPLCallable> fstack = functionStack.get().pop();
                for(ISBPLCallable c : fstack.values()) {
                    if(varLinks.containsKey(c)) {
                        vars.remove(varLinks.remove(c));
                    }
                }
            }
        }
    }
    
    // Magic, please test before pushing changes!
    private String[] splitWords(String code) {
        ArrayList<String> words = new ArrayList<>();
        char[] chars = code.toCharArray();
        boolean isInString = false;
        boolean escaping = false;
        StringBuilder word = new StringBuilder();
        for (int i = 0 ; i < chars.length ; i++) {
            char c = chars[i];
            if(isInString) {
                if(c == '\\') {
                    escaping = !escaping;
                    if(escaping)
                        continue;
                }
                if(c == 'n' && escaping) {
                    word.append('\n');
                    escaping = false;
                    continue;
                }
                if(c == 'r' && escaping) {
                    escaping = false;
                    word.append('\r');
                    continue;
                }
                if(c == '"') {
                    if (escaping) {
                        escaping = false;
                    }
                    else {
                        isInString = false;
                        continue;
                    }
                }
                word.append(c);
                if(escaping)
                    throw new RuntimeException("Error parsing code: Invalid Escape.");
            }
            else if(c == '"' && word.length() == 0) {
                word.append('"');
                isInString = true;
            }
            else if(c == ' ') {
                words.add(word.toString());
                word = new StringBuilder();
            }
            else {
                word.append(c);
            }
        }
        words.add(word.toString());
        return words.toArray(new String[0]);
    }
    
    private String cleanCode(String code) {
        return code
                .replaceAll("\r", "\n")
                .replaceAll("\n", " ")
                ;
    }
    
    public static void main(String[] args) {
        Stack<ISBPLObject> stack = new Stack<>();
        ISBPL isbpl = new ISBPL();
        isbpl.debuggerIPC.stack.put(Thread.currentThread().getId(), stack);
        debug = !System.getenv().getOrDefault("DEBUG", "").equals("");
        isbpl.debuggerIPC.run.put(Thread.currentThread().getId(), -1);
        if(debug) {
            new ISBPLDebugger(isbpl).start();
            isbpl.debuggerIPC.run.put(Thread.currentThread().getId(), 0);
        }
        try {
            File std = new File(System.getenv().getOrDefault("ISBPL_PATH", "/usr/lib/isbpl") + "/std.isbpl");
            isbpl.interpret(std, readFile(std), stack);
            File file = new File(args[0]).getAbsoluteFile();
            isbpl.interpret(file, readFile(file), stack);
            stack.push(argarray(isbpl, args));
            isbpl.interpret(file, "main exit", stack);
        } catch (ISBPLStop stop) {
            System.exit(isbpl.exitCode);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(stack);
        }
    }
    
    private static ISBPLObject argarray(ISBPL isbpl, String[] args) {
        ISBPLObject[] array = new ISBPLObject[args.length - 1];
        for (int i = 1 ; i < args.length ; i++) {
            array[i - 1] = isbpl.toISBPLString(args[i]);
        }
        return new ISBPLObject(isbpl.getType("array"), array);
    }
    
    private static String readFile(File f) throws IOException {
        FileInputStream fis = new FileInputStream(f);
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        byte[] currentBytes = new byte[4096];
        int len;
        while ((len = fis.read(currentBytes)) > 0) {
            bytes.write(currentBytes, 0, len);
        }
        return bytes.toString();
    }
    
}

interface ISBPLKeyword {
    int call(int idx, String[] words, File file, Stack<ISBPLObject> stack);
}

interface ISBPLCallable {
    void call(Stack<ISBPLObject> stack);
}

class ISBPLType {
    int id;
    String name;
    HashMap<String, ISBPLCallable> methods = new HashMap<>();
    HashMap<ISBPLObject, HashMap<Object, ISBPLObject>> vars = new HashMap<>();
    ArrayList<ISBPLType> superTypes = new ArrayList<>();
    
    public ISBPLType(String name, int id) {
        this.name = name;
        this.id = id;
    }
    
    public HashMap<Object, ISBPLObject> varget(ISBPLObject o) {
        if(!vars.containsKey(o)) {
            vars.put(o, new HashMap<>());
        }
        return vars.get(o);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ISBPLType)) return false;
        ISBPLType type = (ISBPLType) o;
        return id == type.id;
    }
    
    @Override
    public int hashCode() {
        return id;
    }
    
    @Override
    public String toString() {
        return "ISBPLType{" +
               "id=" + id +
               ", name='" + name + '\'' +
               '}';
    }
}

class ISBPLObject {
    ISBPLType type;
    Object object;
    
    public ISBPLObject(ISBPLType type, Object object) {
        this.type = type;
        this.object = object;
    }
    
    public boolean isTruthy() {
        return object != null && object != Integer.valueOf(0);
    }
    
    // This has heavy optimizations, please do not change unless necessary
    public boolean equals(Object o) {
        if(this == o) return true;
        if(!(o instanceof ISBPLObject)) return false;
        ISBPLObject object = (ISBPLObject) o;
        if(!this.type.equals(object.type))
            return false;
        if(this.object == object.object)
            return true;
        if(this.object == null)
            return false;
        if(object.object == null)
            return false;
        if(this.object.getClass().isArray() || object.object.getClass().isArray()) {
            if(this.object.getClass().isArray() && object.object.getClass().isArray()) {
                return Arrays.equals((Object[]) this.object, (Object[]) object.object);
            }
            else {
                return false;
            }
        }
        return this.object.equals(object.object);
    }
    
    public void checkType(ISBPLType wanted) {
        if(wanted.id != type.id) {
            throw new ISBPLError("IncompatibleTypes", "Incompatible types: " + type.name + " - " + wanted.name);
        }
    }
    
    public void checkTypeMulti(ISBPLType... wanted) {
        int f = -1;
        StringBuilder wantedNames = new StringBuilder();
        for (int i = 0 ; i < wanted.length ; i++) {
            wantedNames.append(" ").append(wanted[i].name);
            if(wanted[i].id == type.id) {
                f = i;
                break;
            }
        }
        if(f == -1) {
            throw new ISBPLError("IncompatibleTypes", "Incompatible types: " + type.name + " - " + wantedNames.substring(1));
        }
    }
    
    @Override
    public String toString() {
        if(type != null && object instanceof ISBPLObject[]) {
            try {
                return "ISBPLObject{" +
                       "type=" + type +
                       ", object=" + Arrays.toString(((ISBPLObject[]) object)) +
                       '}';
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return "ISBPLObject{" +
               "type=" + type +
               ", object=" + object +
               '}';
    }
    
    public double toDouble() {
        if(object instanceof Integer) {
            return (double) (int) (Integer) object;
        }
        if(object instanceof Long) {
            return (double) (long) (Long) object;
        }
        if(object instanceof Character) {
            return (double) (char) (Character) object;
        }
        if(object instanceof Byte) {
            return Byte.toUnsignedInt((Byte) object);
        }
        if(object instanceof Float) {
            return (double) (float) (Float) object;
        }
        if(object instanceof Double) {
            return (double) (Double) object;
        }
        throw new ISBPLError("InvalidArgument", "The argument is not a number.");
    }
    
    public long toLong() {
        if(object instanceof Integer) {
            return (long) (int) (Integer) object;
        }
        if(object instanceof Long) {
            return (long) (Long) object;
        }
        if(object instanceof Character) {
            return (long) (char) (Character) object;
        }
        if(object instanceof Byte) {
            return Byte.toUnsignedInt((Byte) object);
        }
        if(object instanceof Float) {
            return (long) (float) (Float) object;
        }
        if(object instanceof Double) {
            return (long) (double) (Double) object;
        }
        throw new ISBPLError("InvalidArgument", "The argument is not a number.");
    }
    
    public Object negative() {
        if(object instanceof Integer) {
            return -(int) (Integer) object;
        }
        if(object instanceof Long) {
            return -(long) (Long) object;
        }
        if(object instanceof Float) {
            return -(float) (Float) object;
        }
        if(object instanceof Double) {
            return -(double) (Double) object;
        }
        throw new ISBPLError("InvalidArgument", "This type of number can't be negated!");
    }
}

class ISBPLError extends RuntimeException {
    final String type;
    final String message;
    
    public ISBPLError(String type, String message) {
        this.type = type;
        this.message = message;
    }
    
    @Override
    public String getMessage() {
        return type + ": " + message;
    }
}

class ISBPLStop extends RuntimeException {
    
    int amount;
    
    public ISBPLStop(int amount) {
        this.amount = amount - 1;
    }
}

class ISBPLDebugger extends Thread {
    private final ISBPL isbpl;
    int port = -1;
    long mainID = Thread.currentThread().getId();
    
    public ISBPLDebugger(ISBPL isbpl) {
        this.isbpl = isbpl;
    }
    
    @Override
    public void run() {
        try {
            ServerSocket socket = null;
            try {
                socket = new ServerSocket(Integer.parseInt(System.getenv().getOrDefault("DEBUG", "9736")));
                port = socket.getLocalPort();
                System.err.println("Debugger listening on :" + socket.getLocalPort());
            }
            catch (BindException e) {
                while (socket == null) {
                    try {
                        socket = new ServerSocket((int) (Math.random() * 5000 + 5000));
                        port = socket.getLocalPort();
                        System.err.println("Debugger listening on :" + socket.getLocalPort());
                    }
                    catch (BindException ignored) { }
                }
            }
            isbpl.debuggerIPC.port = port;
            isbpl.debuggerIPC.threadID = Thread.currentThread().getId();
            while (true) {
                Socket s = socket.accept();
                new Thread(() -> {
                    try {
                        BufferedReader reader = new BufferedReader(new InputStreamReader(s.getInputStream()));
                        String line;
                        long tid = mainID;
                        while ((line = reader.readLine()) != null) {
                            try {
                                switch (line.split(" ")[0]) {
                                    case "continue":
                                    case "cont":
                                    case "c":
                                        isbpl.debuggerIPC.run.put(tid, -1);
                                        break;
                                    case "stop":
                                    case "s":
                                        isbpl.debuggerIPC.run.put(tid, 0);
                                        break;
                                    case "next":
                                    case "n":
                                        isbpl.debuggerIPC.run.put(tid, 1);
                                        break;
                                    case "nextall":
                                    case "na":
                                        isbpl.debuggerIPC.run.replaceAll((i, v) -> 1);
                                        break;
                                    case "do":
                                    case "d":
                                        isbpl.debuggerIPC.run.put(tid, Integer.parseInt(line.split(" ")[1]));
                                        break;
                                    case "until":
                                    case "u":
                                        isbpl.debuggerIPC.until = line.split(" ")[1];
                                        isbpl.debuggerIPC.run.put(tid, -2);
                                        break;
                                    case "eval":
                                        isbpl.debuggerIPC.run.put(tid, -3);
                                        isbpl.debuggerIPC.threadID = Thread.currentThread().getId();
                                        try {
                                            isbpl.interpret(new File("_debug").getAbsoluteFile(), line.substring(5), isbpl.debuggerIPC.stack.get(tid));
                                        }
                                        catch (ISBPLStop stop) {
                                            System.exit(isbpl.exitCode);
                                        }
                                        catch (Throwable e) {
                                            e.printStackTrace();
                                            boolean fixed = false;
                                            while (!fixed) {
                                                try {
                                                    System.err.println("Stack recovered: " + isbpl.debuggerIPC.stack.get(tid));
                                                    fixed = true;
                                                }
                                                catch (Exception e1) {
                                                    e.printStackTrace();
                                                    System.err.println("!!! STACK CORRUPTED!");
                                                    isbpl.debuggerIPC.stack.get(tid).pop();
                                                    System.err.println("Popped. Trying again.");
                                                }
                                            }
                                        }
                                        break;
                                    case "dump":
                                        try {
                                            System.err.println("VAR DUMP\n----------------");
                                            for (HashMap<String, ISBPLCallable> map : isbpl.functionStack.map.get(tid)) {
                                                for (String key : map.keySet()) {
                                                    if (key.startsWith("=")) {
                                                        map.get(key.substring(1)).call(isbpl.debuggerIPC.stack.get(tid));
                                                        System.err.println("\t" + key.substring(1) + ": \t" + isbpl.debuggerIPC.stack.get(tid).pop());
                                                    }
                                                }
                                                System.err.println("----------------");
                                            }
                                        }
                                        catch (Exception e) {
                                            e.printStackTrace();
                                            System.err.println("!!! VARS CORRUPTED! CANNOT FIX AUTOMATICALLY.");
                                        }
                                    case "stack":
                                        boolean fixed = false;
                                        while (!fixed) {
                                            try {
                                                System.err.println("STACK DUMP");
                                                for (ISBPLObject object : isbpl.debuggerIPC.stack.get(tid)) {
                                                    System.err.println("\t" + object);
                                                }
                                                fixed = true;
                                            }
                                            catch (Exception e) {
                                                e.printStackTrace();
                                                System.err.println("!!! STACK CORRUPTED!");
                                                isbpl.debuggerIPC.stack.get(tid).pop();
                                                System.err.println("Popped. Trying again.");
                                            }
                                        }
                                        break;
                                    case "son":
                                        ISBPL.debug = true;
                                        ISBPL.printCalls = true;
                                        break;
                                    case "sonf":
                                        ISBPL.debug = false;
                                        ISBPL.printCalls = true;
                                        break;
                                    case "soff":
                                        ISBPL.debug = false;
                                        ISBPL.printCalls = false;
                                        break;
                                    case "exit":
                                        System.exit(255);
                                        break;
                                    case "threads":
                                        System.err.println("THREAD DUMP");
                                        for (Thread thread : Thread.getAllStackTraces().keySet()) {
                                            if (isbpl.debuggerIPC.stack.containsKey(thread.getId()))
                                                System.err.println(thread.getId() + "\t" + thread.getName());
                                        }
                                        break;
                                    case "setthread":
                                    case "st":
                                        long l = Long.parseLong(line.split(" ")[1]);
                                        if (isbpl.debuggerIPC.stack.containsKey(l)) {
                                            tid = l;
                                            System.err.println("Set TID=" + l);
                                        }
                                        else
                                            System.err.println("Thread not valid");
                                        break;
                                    default:
                                        break;
                                }
                            }
                            catch (Exception e) {
                                try {
                                    e.printStackTrace(new PrintStream(s.getOutputStream()));
                                }
                                catch (IOException ex) {
                                    ex.printStackTrace();
                                }
                            }
                        }
                    } catch (IOException e) {
                        try {
                            s.close();
                        }
                        catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    }
                }).start();
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    static class IPC {
        long threadID = -1;
        String until = null;
        HashMap<Long, Integer> run = new HashMap<>();
        HashMap<Long, Stack<ISBPLObject>> stack = new HashMap<>();
        int port = -1;
        
    }
}

class ISBPLStreamer {
    public static final int CREATE_FILE_IN =    0;
    public static final int CREATE_FILE_OUT =   1;
    public static final int CREATE_SOCKET =     2;
    public static final int CLOSE =             3;
    public static final int READ =              4;
    public static final int WRITE =             5;
    @SuppressWarnings("unused")
    public static final int AREAD =             6;
    @SuppressWarnings("unused")
    public static final int AWRITE =            7;
    public static final int CREATE_SERVER =     9;
    
    static class ISBPLStream {
        final InputStream in;
        final OutputStream out;
        static int gid = 0;
        final int id = gid++;
        
        public ISBPLStream(InputStream in, OutputStream out) {
            this.in = in;
            this.out = out;
        }
        
        public void close() throws IOException {
            this.in.close();
            this.out.close();
        }
    }
    
    final ISBPL isbpl;
    
    public ISBPLStreamer(ISBPL isbpl) {
        this.isbpl = isbpl;
    }
    
    public ArrayList<ISBPLStream> streams = new ArrayList<>();
    
    public synchronized void action(Stack<ISBPLObject> stack, int action) throws IOException {
        ISBPLStream stream;
        ISBPLObject s, i;
        File f;
        switch (action) {
            case CREATE_FILE_IN:
                s = stack.pop();
                s.checkType(isbpl.getType("string"));
                f = new File(isbpl.toJavaString(s));
                stream = new ISBPLStream(new FileInputStream(f), new OutputStream() {
                    @Override
                    public void write(int b) {
                        throw new ISBPLError("IllegalArgument", "Can't write to a FILE_IN stream!");
                    }
                });
                streams.add(stream);
                stack.push(new ISBPLObject(isbpl.getType("int"), stream.id));
                break;
            case CREATE_FILE_OUT:
                s = stack.pop();
                s.checkType(isbpl.getType("string"));
                f = new File(isbpl.toJavaString(s));
                stream = new ISBPLStream(new InputStream() {
                    @Override
                    public int read() {
                        throw new ISBPLError("IllegalArgument", "Can't read a FILE_OUT stream!");
                    }
                }, new FileOutputStream(f));
                streams.add(stream);
                stack.push(new ISBPLObject(isbpl.getType("int"), stream.id));
                break;
            case CREATE_SOCKET:
                i = stack.pop();
                s = stack.pop();
                i.checkType(isbpl.getType("int"));
                s.checkType(isbpl.getType("string"));
                Socket socket = new Socket(isbpl.toJavaString(s), ((int) i.object));
                stream = new ISBPLStream(socket.getInputStream(), socket.getOutputStream());
                streams.add(stream);
                stack.push(new ISBPLObject(isbpl.getType("int"), stream.id));
                break;
            case CREATE_SERVER:
                i = stack.pop();
                i.checkType(isbpl.getType("int"));
                ServerSocket server = new ServerSocket(((int) i.object));
                stream = new ISBPLStream(new InputStream() {
                    @Override
                    public int read() throws IOException {
                        Socket socket = server.accept();
                        ISBPLStream stream = new ISBPLStream(socket.getInputStream(), socket.getOutputStream());
                        return stream.id;
                    }
                }, new OutputStream() {
                    @Override
                    public void write(int b) {
                        throw new ISBPLError("IllegalArgument", "Can't write to a SERVER stream!");
                    }
                });
                streams.add(stream);
                stack.push(new ISBPLObject(isbpl.getType("int"), stream.id));
                break;
            case READ:
                i = stack.pop();
                i.checkType(isbpl.getType("int"));
                try {
                    stack.push(new ISBPLObject(isbpl.getType("int"), streams.get(((int) i.object)).in.read()));
                } catch (IndexOutOfBoundsException e) {
                    throw new ISBPLError("IllegalArgument", "streamid STREAM_READ stream called with non-existing stream argument");
                }
                break;
            case WRITE:
                i = stack.pop();
                i.checkType(isbpl.getType("int"));
                ISBPLObject bte = stack.pop();
                bte.checkTypeMulti(isbpl.getType("int"), isbpl.getType("char"), isbpl.getType("byte"));
                try {
                    streams.get(((int) i.object)).out.write(((int) bte.toLong()));
                } catch (IndexOutOfBoundsException e) {
                    throw new ISBPLError("IllegalArgument", "byte streamid STREAM_WRITE stream called with non-existing stream argument");
                }
                break;
            case CLOSE:
                i = stack.pop();
                i.checkType(isbpl.getType("int"));
                try {
                    ISBPLStream strm = streams.get(((int) i.object));
                    strm.close();
                } catch (IndexOutOfBoundsException e) {
                    throw new ISBPLError("IllegalArgument", "streamid STREAM_CLOSE stream called with non-existing stream argument");
                }
                break;
            default:
                throw new ISBPLError("NotImplemented", "Not implemented");
        }
    }
}

class ISBPLThreadLocal<T> {
    HashMap<Long, T> map = new HashMap<>();
    Supplier<? extends T> supplier;
    
    public ISBPLThreadLocal(Supplier<? extends T> supplier) {
        this.supplier = supplier;
    }
    
    public static <T> ISBPLThreadLocal<T> withInitial(Supplier<? extends T> supplier) {
        return new ISBPLThreadLocal<>(supplier);
    }
    
    public T get() {
        long tid = Thread.currentThread().getId();
        if(!map.containsKey(tid))
            map.put(tid, supplier.get());
        return map.get(tid);
    }
    
    public void set(T t) {
        map.put(Thread.currentThread().getId(), t);
    }
}
