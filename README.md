# S3-Proto-Stream

S3-Proto-Stream is a lightweight, Java-based server application designed to emulate a subset of Amazon AWS S3's GetObject functionality. This project is focuses on the handling of GET requests for local and remote files using specific URI formats. It's a simplified version of S3, built for educational purposes to understand underlying network protocols and data streaming techniques.

## Features

- Handles GET requests for files located locally or at specified URLs.
- Supports custom URI formats, mimicking the style of AWS S3 bucket access.
- Capable of fetching specific byte ranges from files, both local and remote.
- Efficiently handles network requests and data streams within constrained memory usage.
- Developed with no standard wrapper classes or libraries instead dealing with raw bytes and int data.

## Getting Started

### Prerequisites

- Java 8

### Installation

1. Clone the repository:
   ```bash
   git clone https://github.com/awr7/S3-Proto-Stream

2. Navigate to the project directory:
   ```bash
   cd S3-Proto-Stream

3. Running the Server

  Compile the Java application:
    ```
    javac S3-Proto-Stream.java
    ```
  Run the server on a specified port:
    ```
    java S3 15123
    ```

## Accessing the Server

To access the server locally, use the following URL format in your browser:

http://localhost:15123/GET/hx://URL-to-fetch&ox=offset&lx=length

or

http://localhost:15123/GET/fx://file1.txt

Replace URL-to-fetch, offset, and length with your desired file URL, byte offset, and length to read, respectively. 

> [!NOTE]
> Is optional to add an offset and a length, if none are specified then the full file will be fetched.

## Usage Examples

Fetch the full file named file1.txt from the local directory where the server is running:

http://localhost:15123/GET/fx://file1.txt

Fetch 16 bytes starting at byte 2473 from alice.txt:

http://localhost:15123/GET/hx://http://gaia.cs.umass.edu/wiresharklabs/alice.tx
