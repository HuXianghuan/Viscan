# Viscan

A JavaFX desktop application for running bioinformatics tools on Windows with WSL2 integration. It provides a GUI for generating command-line pipelines and executing them in embedded terminal tabs.

## Supported Tools

- **fastp** - FASTQ file preprocessing (quality control, filtering, trimming, etc.)
- **bowtie2** - Sequence alignment tool
- **Kraken2** - Taxonomic classification tool
  - k2-classify - Classification
  - k2-build - Database building
  - k2-download-library - Download library files
  - k2-download-taxonomy - Download taxonomy database
- **Recentrifuge** - Classification result visualization tool

## System Requirements

- **OS**: Windows 10/11
- **WSL2**: Installed and configured
- **Java**: JDK 17 or higher
- **Maven**: 3.x
- **Disk Space**: ~3GB for external tools installation

## Quick Start

### 1. Build the Project

```bash
./mvnw clean compile
```

### 2. Run the Application

```bash
./mvnw javafx:run
```

Or using Spring Boot plugin:

```bash
./mvnw spring-boot:run
```

### 3. Install External Tools

Navigate to the **Install** tab and configure the installation parameters:

- **Install script location** - Path to the installation script (pre-filled)
- **Environment location** - Where tools will be installed (default: `external/tools_env/`)
- **Python version** - Python version to use (default: `3.12`)
- **Packages** - Packages to install with optional versions (default: `fastp=1.0.1,bowtie2=2.5.4,kraken2=2.17.1,recentrifuge=2.0.0`)
- **Channels** - Conda channels (default: `conda-forge,bioconda`)
- **Rewrite Python shebangs** - Whether to update Python script shebangs (default: yes)

Click the **Install** button to start the installation. The process runs in a new terminal tab.

To uninstall the tools, click the **Uninstall** button. This will remove the entire `external/tools_env/` directory.

## External Tools Installation

### Command Line Installation (Optional)

The installation script can also be run directly from the command line:

```bash
cd external
bash install.sh [ENV_DIR] [PYTHON_VERSION] [PACKAGES] [CHANNELS] [UPDATE_SHEBANG]
```

**Parameters**:
- `ENV_DIR` - Environment installation directory (default: `./tools_env`)
- `PYTHON_VERSION` - Python version (default: `3.11`)
- `PACKAGES` - Packages to install, comma-separated. You can specify versions like `fastp=1.0.1,bowtie2=2.5.4`
- `CHANNELS` - Conda channels (default: `conda-forge,bioconda`)
- `UPDATE_SHEBANG` - Whether to update Python script shebang (default: `yes`)

## Configuration

Configuration is stored in the user home directory: `~/.viscan.json`

The configuration file is automatically created on first run. Configurable items include:

- WSL executable path
- Executable directories for each tool
- Working directory
- Thread count (auto-detected from CPU cores)
- Output file tags for each tool
- WSL distribution detection

Configuration can be modified through the **Config** tab in the application.

## User Guide

### Basic Workflow

1. **Add Files** - Drag and drop files or directories to the left file list
   - Supports Windows paths (automatically converted to WSL paths)
   - Supports dragging UNC paths and converting to Linux paths

2. **Select Tool** - Choose the bioinformatics tool from the menu or tab bar

3. **Configure Parameters** - Set various parameters in the tool interface

4. **Run Command** - Click the run button to execute the command

5. **View Results** - Check output and results in the embedded terminal tab

### File Path Conversion

The application automatically handles path conversion:
- Windows path (`C:\path`) ↔ Linux path (`/mnt/c/path`)
- Linux path (`/home/user/path`) ↔ Windows UNC path (`\\wsl.localhost\{distro}\home\user\path`)

### Pipeline Execution

Supports chaining multiple commands with `&&` to form pipelines, executing them sequentially.

## Development

### Project Structure

```
src/main/java/com/viscan/
├── Main.java                    # Application entry point
├── controller/                  # FXML controllers
│   ├── BaseTabController.java    # Base class for all tab controllers
│   ├── MainController.java        # Main window controller
│   └── *Controller.java          # Tool-specific controllers
├── tools/                       # Command building system
│   ├── BaseTool.java            # Base class for tool command builders
│   ├── ExternalTool.java        # Interface for external tools
│   ├── CommandPipeline.java      # Command chaining
│   └── option/                  # Command option implementations
├── validate/                    # Input validation
│   ├── ValidationResult.java    # Validation result wrapper
│   ├── ValidationMessages.java   # Error message constants
│   └── *Validator.java          # Tool-specific validators
├── Utils/                       # Utility classes
│   ├── WslPathConverter.java    # Windows/Linux path conversion
│   ├── PathUtils.java           # Path utilities
│   └── PathParts.java           # Path parsing
├── ConfigManager.java           # Configuration management (singleton)
├── BioConfig.java               # Configuration data class
├── FileItem.java                # File representation
├── FileItemStorage.java         # File item storage
├── CommandPage.java             # Tab page wrapper
├── PageRegistry.java            # Page registry
├── DragAcceptors.java           # Drag-and-drop utilities
└── EnviromentManager.java       # External tool path resolution

src/main/resources/com/viscan/
├── *.fxml                      # UI layout files
└── css/listview.css            # Custom styles

external/
├── install.sh                  # Tool installation script
├── micromamba                   # micromamba binary
└── tools_env/                   # Conda environment (tool installation directory)
```

### Adding a New Tool

1. Create a controller class extending `BaseTabController`
2. Create the corresponding FXML file with `fx:controller` attribute
3. Create a validator class in the `validate/` package
4. Add page registration in `PageRegistry` static block
5. Add menu item in `main-view.fxml`
6. Use `BaseTool` and `ToolOption` classes to build commands

### Build Commands

```bash
# Clean and compile
./mvnw clean compile

# Package
./mvnw package

# Run tests
./mvnw test

# Run single test class
./mvnw test -Dtest=ClassName
```

**Note**: Test files are located in `src/main/test/`, not the standard `src/test/`.

## Documentation

- **User Manual**: `docs/viscan_manual.html` - Detailed user guide
- **Developer Guide**: `CLAUDE.md` - Project architecture and development instructions

## Key Features

- **Deep WSL2 Integration** - Seamless execution of Linux bioinformatics tools on Windows
- **Automatic Path Conversion** - Bidirectional conversion between Windows and Linux paths
- **Tabbed Interface** - Each tool has its own tab with embedded terminal
- **Pipelined Commands** - Supports chaining multiple commands with `&&`
- **Input Validation** - Comprehensive validation for each tool's inputs
- **File Management** - Supports drag-and-drop, dual-path storage, grouped display
- **Persistent Configuration** - User settings automatically saved to JSON

## Tech Stack

- **JavaFX** 17.0.16 - GUI framework
- **TerminalFX** 1.2.0 - Embedded terminal support
- **Jackson** 2.20.1 - JSON configuration handling
- **JUnit** 5.10.2 - Unit testing
- **Maven** - Build tool

## License

TBD
