# Simple Makefile for the OnlyJesus Android project

GRADLEW := ./gradlew

.PHONY: help build debug release clean
	
help:
	@echo "Available targets:"
	@echo "  make build          # Build the project (debug by default)"
	@echo "  make debug          # Assemble debug APK"
	@echo "  make release        # Assemble release APK"
	@echo "  make clean          # Clean the build outputs"

build: debug

debug:
	@VERSION_NAME=$$(grep -m1 'versionName' app/build.gradle.kts | sed -E 's/.*versionName *= *"([^"]+)".*/\1/'); \
	if [ -z "$$VERSION_NAME" ]; then \
		echo "Unable to determine versionName from app/build.gradle.kts"; exit 1; \
	fi; \
	echo "Built OnlyJesus v$$VERSION_NAME debug build"; \
	$(GRADLEW) assembleDebug

release:
	@VERSION_NAME=$$(grep -m1 'versionName' app/build.gradle.kts | sed -E 's/.*versionName *= *"([^"]+)".*/\1/'); \
	if [ -z "$$VERSION_NAME" ]; then \
		echo "Unable to determine versionName from app/build.gradle.kts"; exit 1; \
	fi; \
	echo "Built OnlyJesus v$$VERSION_NAME prod release"; \
	$(GRADLEW) assembleRelease

clean:
	$(GRADLEW) clean

