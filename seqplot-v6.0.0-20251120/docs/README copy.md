# SeqPlot v6.0.0

A tool for creating comparison star sequences.

##  Getting Started

### Prerequisites
- Java 8 or higher

### Quick Start
1. Download the latest release: https://drive.google.com/drive/folders/1OjC5ieiwI2HnWbAGVFiyIfBqswVYu1sf?usp=drive_link
where you will find:
a. seqplot-v6.0.0-java8-standalone.jar
b. run-seqplot.bat
c. README_Seqplot600

2. Run the application:

<h3>Option 1</h3>: Double click on the run-seqplot.bat
The bat file will automatically try to find Java and run the application. If it should fail it will provide instructions for Java fixes (and see below)

<h3>Option 2</h3>: open a command prompt: java -jar seqplot-v6.0.0-java8-standalone.jar

This version will work with Java 8, 11, 17 , 21, 25 (not 7 which is pre-2014)

## But it doesn't work...

Obligatory - IWFM :)

<h3>Diagnostic step</h3>: On a powershell: java -version
If this produces an error chances are high that you don't have Java or at least it is not in the path.

Recommendation: Install Java 
Download Java: Visit https://adoptium.net/ and download the latest LTS version of OpenJDK. The website reads "The Power of Eclipse Temurin®
Download Temurin 25 for Windows x64" (Java has a propriety form, this is the open source version - it may not look like what you are expecting but it is legit)
Install Java: Run the installer and make sure to check the option to "Add to PATH" during installation (I believe it is default)
Verify Installation: Open a new PowerShell window and run java -version
Run Seqplot: Once Java is installed, you can simply double-click the run-seqplot.bat file or run it from the command line

<h3>I know I have Java somewhere</h3>: Manual Java Path (If Java is already installed but not in PATH)
If you already have Java installed but it's not in your PATH, you can:

Find your Java installation: Common locations it can be lurking are:

C:\Program Files\Java\
C:\Program Files (x86)\Java\
Run directly: Use the full path to java.exe, for example:
"C:\Program Files\Java\jdk-17\bin\java.exe" -jar seqplot-v6.0.0-java8-standalone.jar


### Building from Source
```bash
# Clone the repository
git clone https://github.com/aegiskeller/seqplot6.git
cd seqplot6

# Build the JAR file
./package.sh

# Run the application
java -jar seqplot-6.0.0.jar
```

##  Project Structure

```
├── src/                    # Source code
│   └── AAVSOtools/        # Main application packages
├── lib/                   # Dependencies (JFreeChart)
├── test_data/            # Sample data files
├── scripts/              # Utility scripts
└── VSDseqs/              # Variable star sequence data
```

##  Development

- **Build**: `./build.sh` (compile only)
- **Package**: `./package.sh` (build + create JAR)


##  Contributing

Contributions welcome! Please read the documentation and test your changes before submitting pull requests.



