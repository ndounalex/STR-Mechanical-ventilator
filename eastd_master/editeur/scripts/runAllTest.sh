#!/usr/bin/env sh

echo "Run all tests"

echo "Test export specification..."
  cd export_tests && node validate_tests.js && cd ..

## when there is gonna be new test add it here