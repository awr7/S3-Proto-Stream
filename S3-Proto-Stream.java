


import java.io.IOException; 
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.io.File;
import java.io.FileInputStream;
import java.net.Socket;
import java.net.ServerSocket;
import java.io.OutputStream;
import java.io.InputStream;
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

int parse(byte[] buf) throws IOException
{
  // fill GET header buffer: /GET/fx://
  byte[] getFxHeaderBytes = {47,71,69,84,47,102,120,58,47,47};
  // /GET/hx://
  byte[] getHxHeaderBytes = {47,71,69,84,47,104,120,58,47,47};



  for (int i = 0; i < buf.length; i++) {
    // Check if the current sequence matches "Host:"
    if (isMatchingSequence(buf, getFxHeaderBytes, i)) {
    // Extract the section of the file from the buffer
    int start = i + getFxHeaderBytes.length;
    int end = start;
    while (end < buf.length && buf[end] != ' ') {
        end++;
    }
    byte[] fileSectionBytes = new byte[end - start];
    for (int j = start, k = 0; j < end; j++, k++) {
        fileSectionBytes[k] = buf[j];
    }

    // get offset and length if they exist!
    long offsetValue = 0;
    long lengthValue = -1;  //we use this later to see if is still = -1 it means that no lx value was there so we can then use file.length

    byte[] oxbytes = new byte[]{38, 111, 120, 61}; // "&ox="
    byte[] lxbytes = new byte[]{38, 108, 120, 61}; // "&lx="
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

    // get the filename
    byte[] filename = new byte[fileSectionBytes.length];
    int filenameLength = 0;
    for (int f = 0; f < fileSectionBytes.length; f++) {
        if (fileSectionBytes[f] == '&' && f + 4 < fileSectionBytes.length && 
            fileSectionBytes[f + 1] == 'o' && fileSectionBytes[f + 2] == 'x' && fileSectionBytes[f + 3] == '=') {
            break; 
        }
        filename[filenameLength++] = fileSectionBytes[f];
    }

    try {
        File file = new File(byte2str(filename, 0, filenameLength));
        if (file.exists()) {
            if (lengthValue == -1) { // If length was not specified
                lengthValue = file.length() - offsetValue;
            }
            streamFileToSocket(file, sock, offsetValue, lengthValue);
            System.out.println("FILE EXIST");
        } else {
            System.out.println("File does not exist.");
        }
    } catch (Exception e) {
        e.printStackTrace();
    }
}

if (isMatchingSequence(buf, getHxHeaderBytes, i)) {
    // Extract the section of the remote file URL from the buffer
    int start = i + getHxHeaderBytes.length;
    int end = start;
    while (end < buf.length && buf[end] != ' ') {
        end++;
    }
    byte[] fileSectionBytes = new byte[end - start];
    for (int j = start, k = 0; j < end; j++, k++) {
        fileSectionBytes[k] = buf[j];
    }

    // Get offset and length if they exist
    long offsetValue = 0;
    long lengthValue = -1;

    byte[] oxbytes = new byte[]{38, 111, 120, 61}; // "&ox="
    byte[] lxbytes = new byte[]{38, 108, 120, 61}; // "&lx="
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

    // Print offset and length if they were found
    if (offsetValue != 0) {
        System.out.println("Offset (Ox): " + offsetValue);
    } else {
        System.out.println("No offset in link!");
    }
    if (lengthValue != -1) {
        System.out.println("Length (Lx): " + lengthValue);
    } else {
        System.out.println("No length in link!");
    }

    // Fetch and stream the file from the remote URL
    fetchAndStreamFileToSocket(fileSectionBytes, sock, offsetValue, lengthValue); // Assuming remote URL is in fileSectionBytes
}
  }
  return 0;
}

int dns(int X) {                         
  InetSocketAddress isa = new InetSocketAddress(byte2str(HOST,0,HOST.length),PORT);
  if (!isa.isUnresolved()) {
  Addr = isa.getAddress();
  } else {
      System.out.println("Hostname could not be resolved.");
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
            System.out.println("Waiting for connections...");
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
      System.out.print(e.getMessage());
    } finally {
      if (s0 != null) {
        try {
          s0.close();
        } catch (Exception e) {
          System.out.println(e.getMessage());
        }
      }
    }
  // } /* while loop */
  int ret = 0;
  return ret;
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
    // Ensure the offset and length are within the bounds of the byte array
  if (i < 0 || j < 0 || i + j > b.length) {
      throw new IllegalArgumentException("Invalid offset or length");
  }
  // Create a new String from the specified range of the byte array
  return new String(b, i, j);
  // byte [] b2 = new byte [1];

  // return new String( b2 );
}

private long byte2long(byte[] b, int offset, int length) {
    if (offset < 0 || length < 0 || offset + length > b.length) {
        throw new IllegalArgumentException("Invalid offset or length");
    }

    long result = 0;
    for (int i = offset; i < offset + length; i++) {
        if (b[i] < '0' || b[i] > '9') {
            System.out.println("Invalid byte at position " + i + ": " + b[i] + " (char representation: " + (char) b[i] + ")");
            throw new IllegalArgumentException("Invalid byte for number conversion");
        }
        int digit = b[i] - '0';
        result = result * 10 + digit;
    }
    return result;
}

byte[] extractBytes(byte[]b, int i, int j)
{
  // return a trunkated byte array, removing bytes starting at offset i and of length j
  // Check if the indices are valid
  if (i < 0 || j < 0 || i + j > b.length) {
      throw new IllegalArgumentException("Invalid index or length");
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

private void streamFileToSocket(File file, Socket socket, long offset, long length) {
    if (socket == null || socket.isClosed() || !socket.isConnected()) {
        System.out.println("Socket is closed, null, or not connected.");
        return;
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
                throw new IOException("Unable to skip the desired number of bytes");
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
        System.out.println("HTTP response sent!");
        outputStream.flush();

    } catch (IOException e) {
        System.out.println("IO Exception: " + e.getMessage());
    }
}

private void fetchAndStreamFileToSocket(byte[] fileURLBytes, Socket clientSocket, long offset, long length) throws IOException {
    byte[] hostname = getHost(fileURLBytes);
    byte[] path = getPath(fileURLBytes);

    // Set the HOST global variable and call dns to resolve the IP address
    HOST = hostname;
    dns(0);

    byte[] getRequest = makeGetRequest(path, hostname);

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

private void sendContent(InputStream serverIn, OutputStream clientOut, long offset, long length) throws IOException {
    if (offset == 0 && length < 0) {
        // Full content: stream directly, including server headers
        byte[] buffer = new byte[4096];
        int bytesRead;
        while ((bytesRead = serverIn.read(buffer)) != -1) {
            clientOut.write(buffer, 0, bytesRead);
        }
    } else {
        //Skiip header we got from the get request
        skipHeader(serverIn); 

        // Create and send new header for partial content
        byte[] headers = createHeaders(length);
        clientOut.write(headers);
        clientOut.flush();

        // Skip offset
        long skippedOffset = serverIn.skip(offset);
        System.out.println("Skipped offset bytes: " + skippedOffset);
        if (skippedOffset < offset) {
            throw new IOException("Unable to skip the desired number of bytes");
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
        'H', 'T', 'T', 'P', '/', '1', '.', '1', ' ', '2', '0', '0', ' ', 'O', 'K', '\r', '\n',
        'C', 'o', 'n', 't', 'e', 'n', 't', '-', 'T', 'y', 'p', 'e', ':', ' ', 't', 'e', 'x', 't', '/', 'p', 'l', 'a', 'i', 'n', '\r', '\n',
        // Content-Length: 
        'C', 'o', 'n', 't', 'e', 'n', 't', '-', 'L', 'e', 'n', 'g', 't', 'h', ':', ' '
    };
    byte[] contentLengthBytes = longToBytes(contentLength);
    byte[] endHeaders = {
        // Connection: close and new lines
        '\r', '\n', 'C', 'o', 'n', 'n', 'e', 'c', 't', 'i', 'o', 'n', ':', ' ', 'c', 'l', 'o', 's', 'e', '\r', '\n', '\r', '\n'
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
        'U', 'n', 'a', 'b', 'l', 'e', ' ', 
        't', 'o', ' ', 
        's', 'k', 'i', 'p', ' ', 
        't', 'h', 'e', ' ', 
        'd', 'e', 's', 'i', 'r', 'e', 'd', ' ', 
        'n', 'u', 'm', 'b', 'e', 'r', ' ', 
        'o', 'f', ' ', 
        'b', 'y', 't', 'e', 's'
    };
}

private byte[] longToBytes(long value) {
    if (value == 0) return new byte[] {'0'};

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
        result[i--] = (byte) ('0' + digit);
        value /= 10;
    }
    return result;
}

private long getOffset(byte[] fileSectionBytes) {
    byte[] oxbytes = new byte[] {38, 111, 120, 61}; // "&ox="
    int oxStart = findSequence(fileSectionBytes, oxbytes) + oxbytes.length;
    int oxEnd = findNextDelimiter(fileSectionBytes, oxStart, (byte)'&');
    byte[] offsetBytes = new byte[oxEnd - oxStart];
    for (int i = oxStart, k = 0; i < oxEnd; i++, k++) {
        offsetBytes[k] = fileSectionBytes[i];
    }
    return byte2long(offsetBytes, 0, offsetBytes.length);
}

private long getLength(byte[] fileSectionBytes) {
    byte[] lxbytes = new byte[] {38, 108, 120, 61}; // "&lx="
    int lxStart = findSequence(fileSectionBytes, lxbytes) + lxbytes.length;
    int lxEnd = findNextDelimiter(fileSectionBytes, lxStart, (byte)'&');
    byte[] lengthBytes = new byte[lxEnd - lxStart];
    for (int i = lxStart, k = 0; i < lxEnd; i++, k++) {
        lengthBytes[k] = fileSectionBytes[i];
    }
    return byte2long(lengthBytes, 0, lengthBytes.length);
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
    return source.length; // Return the array length if no delimiter is found
}

private byte[] getHost(byte[] url) {
    byte[] protocolSeparator = new byte[] {58, 47, 47}; // "://"

    int start = indexOf(url, protocolSeparator) + protocolSeparator.length;
    if (start < protocolSeparator.length) return new byte[0]; // "://" not found

    // Find the end of the hostname (before any '/' or '&' or ' ')
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
    // Find the start of the path (first '/' after "://")
    int start = indexOf(url, (byte) 47, indexOf(url, new byte[] {58, 47, 47}) + 3); // 58, 47, 47 = "://"
    if (start == -1) return new byte[0]; // No '/' found after "://"

    // Find the end of the path (before any '&' or ' ')
    int end = start;
    while (end < url.length && url[end] != '&' && url[end] != ' ') {
        end++;
    }

    byte[] path = new byte[end - start];
    for (int i = start, k = 0; i < end; i++, k++) {
        path[k] = url[i];
    }
    return path;
}

// For a single byte "target"
private int indexOf(byte[] source, byte target, int fromIndex) {
    for (int i = fromIndex; i < source.length; i++) {
        if (source[i] == target) {
            return i;
        }
    }
    return -1;
}
// For a sequence of bytes 
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

private byte[] makeGetRequest(byte[] path, byte[] hostname) {
    byte[] getRequestStart = {
        'G', 'E', 'T', ' '
    };
    byte[] getRequestMiddle = {
        ' ', 'H', 'T', 'T', 'P', '/', '1', '.', '1', '\r', '\n',
        'H', 'o', 's', 't', ':', ' '
    };
    byte[] getRequestEnd = {
        '\r', '\n', '\r', '\n'
    };

    // Calculate total length
    int totalLength = getRequestStart.length + path.length + getRequestMiddle.length + hostname.length + getRequestEnd.length;

    byte[] getRequest = new byte[totalLength];
    int position = 0;

    // Copy getRequestStart
    for (byte b : getRequestStart) {
        getRequest[position++] = b;
    }

    // Copy path
    for (byte b : path) {
        getRequest[position++] = b;
    }

    // Copy getRequestMiddle
    for (byte b : getRequestMiddle) {
        getRequest[position++] = b;
    }

    // Copy hostname
    for (byte b : hostname) {
        getRequest[position++] = b;
    }

    // Copy getRequestEnd
    for (byte b : getRequestEnd) {
        getRequest[position++] = b;
    }

    return getRequest;
}

private void skipHeader(InputStream in) throws IOException {
    int readByte = 0;
    int newLineSeqCount = 0;


    while (newLineSeqCount < 4) {
        readByte = in.read();
        if (readByte == -1) break;


        if ((readByte == 13 && newLineSeqCount % 2 == 0) || (readByte == 10 && newLineSeqCount % 2 != 0)) {
            newLineSeqCount++; // find '\r' or '\n' in the sequence
        } else {
            newLineSeqCount = 0; // Reset if sequence is broken
        }
    }
}

/* --- end of all methods --- */
} /* class S3 */
