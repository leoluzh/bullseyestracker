# BullseyesTracker - common project commands.
# Wraps Gradle; works with either ./gradlew (once generated via `make wrapper`) or a
# devbox-provided `gradle` on PATH.
#
# Android SDK note: devbox.json supplies JDK 17 + Gradle + adb, but NOT the full Android SDK
# (compileSdk 34 platform + build-tools) - nixpkgs' androidenv license-acceptance option does
# not propagate through devbox (jetify-com/devbox#2236). Set ANDROID_HOME/ANDROID_SDK_ROOT to
# an Android Studio (or standalone cmdline-tools + sdkmanager) install before building.

GRADLE := $(shell test -x ./gradlew && echo ./gradlew || echo gradle)

.PHONY: help
help:
	@echo "BullseyesTracker - available targets:"
	@echo "  make doctor           check JDK/Gradle/adb/ANDROID_HOME are visible"
	@echo "  make wrapper          generate ./gradlew (requires system 'gradle', e.g. via devbox)"
	@echo "  make build            build all modules"
	@echo "  make assemble-debug   build the debug APK (app module)"
	@echo "  make assemble-release build the release APK (app module)"
	@echo "  make test             run unit tests (cv + match + app, JVM-only)"
	@echo "  make connected-test   run instrumented tests (needs a connected device/emulator)"
	@echo "  make lint             run ktlint check"
	@echo "  make format           run ktlint format (auto-fix)"
	@echo "  make install          install debug APK on a connected device"
	@echo "  make run              install + launch the app on a connected device"
	@echo "  make clean            clean all build outputs"

.PHONY: doctor
doctor:
	@echo "--- java ---"; java -version
	@echo "--- gradle ---"; $(GRADLE) --version
	@echo "--- adb ---"; adb version || echo "adb not found on PATH"
	@echo "--- ANDROID_HOME ---"; echo "$${ANDROID_HOME:-<not set>}"
	@echo "--- ANDROID_SDK_ROOT ---"; echo "$${ANDROID_SDK_ROOT:-<not set>}"

.PHONY: wrapper
wrapper:
	gradle wrapper --gradle-version 8.9

.PHONY: build
build:
	$(GRADLE) build

.PHONY: assemble-debug
assemble-debug:
	$(GRADLE) :app:assembleDebug

.PHONY: assemble-release
assemble-release:
	$(GRADLE) :app:assembleRelease

.PHONY: test
test:
	$(GRADLE) test

.PHONY: connected-test
connected-test:
	$(GRADLE) connectedAndroidTest

.PHONY: lint
lint:
	$(GRADLE) ktlintCheck

.PHONY: format
format:
	$(GRADLE) ktlintFormat

.PHONY: install
install:
	$(GRADLE) :app:installDebug

.PHONY: run
run: install
	adb shell am start -n com.bullseyestracker/.MainActivity

.PHONY: clean
clean:
	$(GRADLE) clean
