#!/usr/bin/env bash
set -e

DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$DIR"

echo "=========================================================="
echo "  Equity Stock Exchange & Portfolio Management Platform"
echo "=========================================================="

mkdir -p bin

echo "Compiling Java source and test files..."
javac -d bin $(find src/main/java src/test/java -name "*.java")

case "$1" in
    cli)
        echo "Launching interactive terminal console (CLI mode)..."
        java -cp bin com.exchange.Main --cli
        ;;
    test)
        echo "Running automated verification tests..."
        java -cp bin com.exchange.TradingPlatformTest
        ;;
    docs)
        echo "Generating Javadoc documentation..."
        javadoc -d docs -sourcepath src/main/java -subpackages com.exchange
        echo "Docs generated in docs/index.html"
        ;;
    graph)
        echo "Updating Graphify knowledge graph..."
        /Users/mridulgupta2911/.local/bin/graphify extract . --code-only
        /Users/mridulgupta2911/.local/bin/graphify export html
        echo "Graph updated in graphify-out/graph.html"
        ;;
    gui|"")
        echo "Launching modern desktop GUI trading workstation..."
        java -cp bin com.exchange.Main
        ;;
    *)
        echo "Unknown option '$1'. Usage: ./run.sh [gui|cli|test|docs|graph]"
        exit 1
        ;;
esac
