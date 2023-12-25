import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.Socket;
import java.net.ServerSocket;
import java.io.OutputStream;
import java.io.InputStream;
import java.io.PrintStream;
/* -- end of imports -- */

class S3 {

  private byte []     HOST;
  private int         PORT; 
  private InetAddress Addr;
  private Socket sock; // client socket to send the filestream to
public static void main(String [] a) {   
  S3 bucket = new S3(Integer.parseInt(a[0]));
  bucket.run(0);
}

public S3 (int port) {
  PORT = port;
 }

int parse(byte[] buf) throws Exception
{
  byte[] getFxHeaderBytes = {47,71,69,84,47,102,120,58,47,47};

  byte[] getHxHeaderBytes = {47,71,69,84,47,104,120,58,47,47};

  for (int i = 0; i < buf.length; i++) {
    if (isMatchingSequence(buf, getFxHeaderBytes, i)) {
    int start = i + getFxHeaderBytes.length;
    int end = start;
    while (end < buf.length && buf[end] != ' ') {
        end++;
    }
    byte[] fileSectionBytes = new byte[end - start];
    for (int j = start, k = 0; j < end; j++, k++) {
        fileSectionBytes[k] = buf[j];
    }

    long offsetValue = 0;
    long lengthValue = -1; 

    byte[] oxbytes = new byte[]{38, 111, 120, 61};
    byte[] lxbytes = new byte[]{38, 108, 120, 61}; 
    for (int z = 0; z < fileSectionBytes.length; z++) {
        if (isMatchingSequence(fileSectionBytes, oxbytes, z)) {
            offsetValue = getOffset(fileSectionBytes);
            break;
        }
    }
    for (int z = 0; z < fileSectionBytes.length; z++) {
        if (isMatchingSequence(fileSectionBytes, lxbytes, z)) {
            lengthValue = getLength(fileSectionBytes);
            break;
        }
    }
    byte[] filename = new byte[fileSectionBytes.length];
    int filenameLength = 0;
    for (int f = 0; f < fileSectionBytes.length; f++) {
        if (fileSectionBytes[f] == 0x26 && f + 4 < fileSectionBytes.length && 
            fileSectionBytes[f + 1] == 0x6F && fileSectionBytes[f + 2] == 0x78 && 
            fileSectionBytes[f + 3] == 0x3D)
        filename[filenameLength++] = fileSectionBytes[f];
    }
    try {
        File file = new File(byte2str(filename, 0, filenameLength));
        if (file.exists()) {
            if (lengthValue == -1) { // If length was not specified
                lengthValue = file.length() - offsetValue;
            }
            streamFileToSocket(file, sock, offsetValue, lengthValue);
        } else {
            throw new Exception();
        }
    } catch (Exception e) {
        e.printStackTrace();
    }
}

if (isMatchingSequence(buf, getHxHeaderBytes, i)) {
    int start = i + getHxHeaderBytes.length;
    int end = start;
    while (end < buf.length && buf[end] != ' ') {
        end++;
    }
    byte[] fileSectionBytes = new byte[end - start];
    for (int j = start, k = 0; j < end; j++, k++) {
        fileSectionBytes[k] = buf[j];
    }
    long offsetValue = 0;
    long lengthValue = -1;
    byte[] oxbytes = new byte[]{38, 111, 120, 61}; 
    byte[] lxbytes = new byte[]{38, 108, 120, 61}; 
    for (int z = 0; z < fileSectionBytes.length; z++) {
        if (isMatchingSequence(fileSectionBytes, oxbytes, z)) {
            offsetValue = getOffset(fileSectionBytes);
             break;
        }
    }
    for (int z = 0; z < fileSectionBytes.length; z++) {
        if (isMatchingSequence(fileSectionBytes, lxbytes, z)) {
            lengthValue = getLength(fileSectionBytes);
            break;
        }
    }
    fetchAndStreamFileToSocket(fileSectionBytes, sock, offsetValue, lengthValue); 
}
  }
  return HOST.length;
}

int dns(int X) throws Exception {                         
  InetSocketAddress isa = new InetSocketAddress(byte2str(HOST,0,HOST.length),PORT);
  if (!isa.isUnresolved()) {
  Addr = isa.getAddress();
  } else {
        throw new Exception();
  }
  return X;
}

int run(int X)                           
{
  ServerSocket s0 = null;

    try {
      s0 = new ServerSocket(PORT);
      while (true) {
        try {
            byte [] b0 = new byte[1024]; // temp buffer size
            sock = s0.accept();

            int bytesRead = sock.getInputStream().read(b0,0,b0.length);
            if (bytesRead > 0) {
                // Process the request
                int hostLength = parse(b0);
                dns(X); // this sets the ADDR
            }
        } catch (Exception e) {
            e.printStackTrace();
        } 
      }
    } catch (Exception e) {
        e.printStackTrace();
    } finally {
      if (s0 != null) {
        try {
          s0.close();
        } catch (Exception e) {
        e.printStackTrace();
        }
      }
    }
  return 0;
} /* run */

private static boolean isMatchingSequence(byte[] source, byte[] target, int start) {
    if (start + target.length > source.length) {
        return false;
    }
    for (int i = 0; i < target.length; i++) {
        if (source[start + i] != target[i]) {
            return false;
        }
    }
    return true;
}

String byte2str(byte []b, int i, int j)
{
  // byte []b = array of characters
  // int i = offset index
  // int j = length
  byte[] errorMsg = {
    73, 110, 118, 97, 108, 105, 100, 32, 
    111, 102, 102, 115, 101, 116, 32, 111, 
    114, 32, 108, 101, 110, 103, 116, 104
    };

    // Ensure the offset and length are within the bounds of the byte array
  if (i < 0 || j < 0 || i + j > b.length) {
      throw new IllegalArgumentException(byte2str(errorMsg,0,errorMsg.length));
  }
  // Create a new String from the specified range of the byte array
  return new String(b, i, j);
}

private long byte2long(byte[] b, int offset, int length) throws Exception{
    if (offset < 0 || length < 0 || offset + length > b.length) {
        throw new Exception();
    }

    long result = 0;
    for (int i = offset; i < offset + length; i++) {
        if (b[i] < 48 || b[i] > 57) {
        throw new Exception();
        }
        int digit = b[i] - 48;
        result = result * 10 + digit;
    }
    return result;
}

byte[] extractBytes(byte[]b, int i, int j) throws Exception
{
  // return a trunkated byte array, removing bytes starting at offset i and of length j
  // Check if the indices are valid
  if (i < 0 || j < 0 || i + j > b.length) {
              throw new Exception();
  }

  // Calculate the length of the new array
  int newLength = b.length - j;
  byte[] t = new byte[newLength];

  // Manually copy the bytes
  int currentIndex = 0;
  for (int index = 0; index < b.length; index++) {
      if (index < i || index >= i + j) {
          t[currentIndex++] = b[index];
      }
  }
  return t;
}

private void streamFileToSocket(File file, Socket socket, long offset, long length) throws Exception{
    if (socket == null || socket.isClosed() || !socket.isConnected()) {
        throw new Exception();
    }

    try (FileInputStream fileInputStream = new FileInputStream(file);
         OutputStream outputStream = socket.getOutputStream()) {

        if (offset > file.length()) {
            byte[] headers = createHeaders(42);
            outputStream.write(headers);
            byte[] errorMessage = createErrorMessage();
            outputStream.write(errorMessage);
            outputStream.flush();
            return;
        }

        // Skip to the specified offset in the file
        long skippedBytes = 0;
        while (skippedBytes < offset) {
            long result = fileInputStream.skip(offset - skippedBytes);
            if (result <= 0) {
        throw new Exception();
            }
            skippedBytes += result;
        }

        // Prepare headers
        byte[] headers = createHeaders(length);
        outputStream.write(headers);

        // Send file content
        byte[] buffer = new byte[4096];
        int bytesRead;
        long totalBytesRead = 0;
        while (totalBytesRead < length) {
            int bytesToRead = Min(buffer.length, (int)(length - totalBytesRead));
            bytesRead = fileInputStream.read(buffer, 0, bytesToRead);
            if (bytesRead == -1) {
                break;
            }

            outputStream.write(buffer, 0, bytesRead);
            totalBytesRead += bytesRead;
        }
        outputStream.flush();

    } catch (Exception e) {
        e.printStackTrace();
    }
}

private static int assignBytes(byte[] source, byte[] destination, int startPos) {
    for (int i = 0; i < source.length; i++) {
        destination[startPos + i] = source[i];
    }
    return startPos + source.length;
}

public static byte[] createHttpGetRequest(byte[] filePath, byte[] hostName) {
    byte[] method = {(byte) 0x47, (byte) 0x45, (byte) 0x54, (byte) 0x20}; 
    byte[] httpVersion = {(byte) 0x20, (byte) 0x48, (byte) 0x54, (byte) 0x54, (byte) 0x50, 
                          (byte) 0x2F, (byte) 0x31, (byte) 0x2E, (byte) 0x31, (byte) 0x0D, (byte) 0x0A}; 
    byte[] hostHeaderPrefix = {(byte) 0x48, (byte) 0x6F, (byte) 0x73, (byte) 0x74, (byte) 0x3A, 
                               (byte) 0x20}; 
    byte[] newLine = {(byte) 0x0D, (byte) 0x0A}; 

    int totalLength = method.length + filePath.length + httpVersion.length +
                      hostHeaderPrefix.length + hostName.length + 2 * newLine.length;

    byte[] request = new byte[totalLength];

    int pos = 0;
    pos = assignBytes(method, request, pos);
    pos = assignBytes(filePath, request, pos);
    pos = assignBytes(httpVersion, request, pos);
    pos = assignBytes(hostHeaderPrefix, request, pos);
    pos = assignBytes(hostName, request, pos);
    pos = assignBytes(newLine, request, pos);
    assignBytes(newLine, request, pos); 

    return request;
}

private void fetchAndStreamFileToSocket(byte[] fileURLBytes, Socket clientSocket, long offset, long length) throws Exception {
    byte[] hostname = getHost(fileURLBytes);
    byte[] path = getPath(fileURLBytes);

    // Set the HOST global variable and call dns to resolve the IP address
    HOST = hostname;
    dns(0);

    byte[] getRequest = createHttpGetRequest(path,hostname);


    try (Socket serverSocket = new Socket(Addr, 80); // default http port
         OutputStream serverOut = serverSocket.getOutputStream();
         InputStream serverIn = serverSocket.getInputStream();
         OutputStream clientOut = clientSocket.getOutputStream()) {

        // Send the GET request
        serverOut.write(getRequest);
        serverOut.flush();
        sendContent(serverIn, clientOut, offset, length);
        }
}

private void sendContent(InputStream serverIn, OutputStream clientOut, long offset, long length) throws Exception {
    if (offset == 0 && length < 0) {
        // Full content: stream directly, including server headers
        byte[] buffer = new byte[4096];
        int bytesRead;
        while ((bytesRead = serverIn.read(buffer)) != -1) {
            clientOut.write(buffer, 0, bytesRead);
        }
    } else {
        //Skiip header we got from the get request
        // skipHeader(serverIn); 
        int contentLength = skipHeader(serverIn);
        // Now you can use contentLength as needed

        // Create and send new header for partial content
        byte[] headers = createHeaders(length);
        clientOut.write(headers);
        clientOut.flush();

        // Skip offset
        long skippedOffset = serverIn.skip(offset);
        if (skippedOffset < offset) {
            throw new Exception();
        }
        // send partial content
        byte[] buffer = new byte[4096];
        int bytesRead;
        long totalRead = 0;
        while (totalRead < length && (bytesRead = serverIn.read(buffer, 0, Min(buffer.length, (int)(length - totalRead)))) != -1) {
            clientOut.write(buffer, 0, bytesRead);
            totalRead += bytesRead;
        }
    }
    clientOut.flush();
}

private int Min(int a, int b) {
    return (a < b) ? a : b;
}

private byte[] createHeaders(long contentLength) {
    byte[] startHeaders = {
        // HTTP/1.1 200 OK and Content-Type: text/plain
        0x48, 0x54, 0x54, 0x50, 0x2F, 0x31, 0x2E, 0x31, 0x20, 0x32, 0x30, 0x30, 0x20, 0x4F, 0x4B, 0x0D, 0x0A,
        0x43, 0x6F, 0x6E, 0x74, 0x65, 0x6E, 0x74, 0x2D, 0x54, 0x79, 0x70, 0x65, 0x3A, 0x20, 0x74, 0x65, 0x78, 0x74, 0x2F, 0x70, 0x6C, 0x61, 0x69, 0x6E, 0x0D, 0x0A,
        // Content-Length:
        0x43, 0x6F, 0x6E, 0x74, 0x65, 0x6E, 0x74, 0x2D, 0x4C, 0x65, 0x6E, 0x67, 0x74, 0x68, 0x3A, 0x20
    };
    byte[] contentLengthBytes = longToBytes(contentLength);
    byte[] endHeaders = {
        // Connection: close and new lines
        0x0D, 0x0A, 0x43, 0x6F, 0x6E, 0x6E, 0x65, 0x63, 0x74, 0x69, 0x6F, 0x6E, 0x3A, 0x20, 0x63, 0x6C, 0x6F, 0x73, 0x65, 0x0D, 0x0A, 0x0D, 0x0A
    };

    byte[] headers = new byte[startHeaders.length + contentLengthBytes.length + endHeaders.length];

    // Manually copy the bytes
    int position = 0;
    for (byte b : startHeaders) {
        headers[position++] = b;
    }
    for (byte b : contentLengthBytes) {
        headers[position++] = b;
    }
    for (byte b : endHeaders) {
        headers[position++] = b;
    }

    return headers;
}

private byte[] createErrorMessage() {
    return new byte[] {
        // Unable to skip the desired number of bytes
        0x55, 0x6E, 0x61, 0x62, 0x6C, 0x65, 0x20, // Unable 
        0x74, 0x6F, 0x20, // to 
        0x73, 0x6B, 0x69, 0x70, 0x20, // skip 
        0x74, 0x68, 0x65, 0x20, // the 
        0x64, 0x65, 0x73, 0x69, 0x72, 0x65, 0x64, 0x20, // desired 
        0x6E, 0x75, 0x6D, 0x62, 0x65, 0x72, 0x20, // number 
        0x6F, 0x66, 0x20, // of 
        0x62, 0x79, 0x74, 0x65, 0x73 // bytes
    };
}

private byte[] longToBytes(long value) {
    if (value == 0) return new byte[] {48};

    // find the num of digits in our value
    int length = 0;
    long tempValue = value;
    while (tempValue > 0) {
        length++;
        tempValue /= 10;
    }

    byte[] result = new byte[length];
    int i = length - 1;
    while (value > 0) {
        long digit = value % 10;
        result[i--] = (byte) (48 + digit);
        value /= 10;
    }
    return result;
}

private long getOffset(byte[] fileSectionBytes) {
    byte[] oxbytes = new byte[] {38, 111, 120, 61};
    int oxStart = findSequence(fileSectionBytes, oxbytes) + oxbytes.length;
    int oxEnd = findNextDelimiter(fileSectionBytes, oxStart, (byte)38);
    byte[] offsetBytes = new byte[oxEnd - oxStart];
    for (int i = oxStart, k = 0; i < oxEnd; i++, k++) {
        offsetBytes[k] = fileSectionBytes[i];
    }
    try {
        return byte2long(offsetBytes, 0, offsetBytes.length);
    } catch (Exception e) {
        e.printStackTrace();
        return -1;
    }
}

private long getLength(byte[] fileSectionBytes) {
    byte[] lxbytes = new byte[] {38, 108, 120, 61}; 
    int lxStart = findSequence(fileSectionBytes, lxbytes) + lxbytes.length;
    int lxEnd = findNextDelimiter(fileSectionBytes, lxStart, (byte)38);
    byte[] lengthBytes = new byte[lxEnd - lxStart];
    for (int i = lxStart, k = 0; i < lxEnd; i++, k++) {
        lengthBytes[k] = fileSectionBytes[i];
    }
    try {
           return byte2long(lengthBytes, 0, lengthBytes.length);
 
    }
    catch (Exception e) {
        e.printStackTrace();
        return -1;
    }
}

private int findSequence(byte[] source, byte[] sequence) {
    if (sequence.length == 0) return -1;
    for (int i = 0; i < source.length - sequence.length + 1; i++) {
        boolean found = true;
        for (int j = 0; j < sequence.length; j++) {
            if (source[i + j] != sequence[j]) {
                found = false;
                break;
            }
        }
        if (found) return i;
    }
    return -1;
}

private int findNextDelimiter(byte[] source, int start, byte delimiter) {
    for (int i = start; i < source.length; i++) {
        if (source[i] == delimiter) {
            return i;
        }
    }
    return source.length; 
}

private byte[] getHost(byte[] url) {
    byte[] protocolSeparator = new byte[] {58, 47, 47}; 

    int start = indexOf(url, protocolSeparator) + protocolSeparator.length;
    if (start < protocolSeparator.length) return new byte[0]; 

    int end = start;
    while (end < url.length && url[end] != 47 && url[end] != 38 && url[end] != 32) {
        end++;
    }

    byte[] hostname = new byte[end - start];
    for (int i = start, k = 0; i < end; i++, k++) {
        hostname[k] = url[i];
    }
    return hostname;
}

private byte[] getPath(byte[] url) {

    int start = indexOf(url, (byte) 47, indexOf(url, new byte[] {58, 47, 47}) + 3); 
    if (start == -1) return new byte[0]; 

   
    int end = start;
    while (end < url.length && url[end] != 38 && url[end] != 32) {
        end++;
    }

    byte[] path = new byte[end - start];
    for (int i = start, k = 0; i < end; i++, k++) {
        path[k] = url[i];
    }
    return path;
}

private int indexOf(byte[] source, byte target, int fromIndex) {
    for (int i = fromIndex; i < source.length; i++) {
        if (source[i] == target) {
            return i;
        }
    }
    return -1;
}

private int indexOf(byte[] source, byte[] target) {
    if (target.length == 0 || source.length == 0) return -1;
    for (int i = 0; i <= source.length - target.length; i++) {
        boolean found = true;
        for (int j = 0; j < target.length; j++) {
            if (source[i + j] != target[j]) {
                found = false;
                break;
            }
        }
        if (found) return i;
    }
    return -1;
}

private int skipHeader(InputStream in) throws Exception {
    int readByte;
    int newLineSeqCount = 0;
    boolean isContentLength = false;
    int contentLength = 0;
    int[] contentLengthKey = new int[]{99, 111, 110, 116, 101, 110, 116, 45, 108, 101, 110, 103, 116, 104, 58};
    int contentLengthKeyIndex = 0;

    while (newLineSeqCount < 4) {
        readByte = in.read();
        if (readByte == -1) break;
       
        if (readByte == contentLengthKey[contentLengthKeyIndex]) {
            contentLengthKeyIndex++;
            if (contentLengthKeyIndex == contentLengthKey.length) {
                isContentLength = true;
                contentLengthKeyIndex = 0; 
            }
        } else {
            contentLengthKeyIndex = 0; 
        }

        if (isContentLength) {
            if (readByte >= 48 && readByte <= 57) { 
                contentLength = contentLength * 10 + (readByte - 48); 
            } else if (readByte == 13 || readByte == 10) { 
                isContentLength = false; 
            }
        }

        if ((readByte == 13 && newLineSeqCount % 2 == 0) || (readByte == 10 && newLineSeqCount % 2 != 0)) {
            newLineSeqCount++; 
        } else {
            newLineSeqCount = 0; 
        }
    }

    return contentLength;
}

/* --- end of all methods --- */
} /* class S3 */