# SeqPlot v6.0.0

🌟 **Astronomical Sequence Plotting Tool for AAVSO**

<!-- Add a screenshot here -->
![SeqPlot Application](images/seqplot-screenshot.png)

## ✨ Features

- 🔭 **Multi-Catalog Support**: GAIA, SDSS, PanSTARRS integration
- 🎨 **Enhanced UI**: Warm pastel color scheme with modern styling
- 📊 **Advanced Plotting**: Interactive star charts with magnitude sequences
- 🖼️ **Image Integration**: DSS2 background images with coordinate grids
- 📱 **User-Friendly**: Intuitive interface for astronomical data visualization

## 🚀 Getting Started

### Prerequisites
- Java 8 or higher
- Internet connection for catalog queries

### Quick Start
1. Download the latest release: `seqplot-6.0.0.jar`
2. Run the application:
   ```bash
   java -jar seqplot-6.0.0.jar
   ```

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

## 📁 Project Structure

```
├── src/                    # Source code
│   └── AAVSOtools/        # Main application packages
├── lib/                   # Dependencies (JFreeChart)
├── test_data/            # Sample data files
├── scripts/              # Utility scripts
└── VSDseqs/              # Variable star sequence data
```

## 🛠️ Development

- **Build**: `./build.sh` (compile only)
- **Package**: `./package.sh` (build + create JAR)
- **Test**: Various test scripts available in project root

## 📖 Documentation

See `README_v6.0.0.md` for detailed version information and `DEBUG_LOG_REFERENCE.md` for troubleshooting.

## 🤝 Contributing

Contributions welcome! Please read the documentation and test your changes before submitting pull requests.

## 📄 License

AAVSO Tools Project
