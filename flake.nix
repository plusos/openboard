{
  description = "OpenBoard Android keyboard development environment";

  inputs = {
    nixpkgs.url = "github:NixOS/nixpkgs/nixos-unstable";
    flake-utils.url = "github:numtide/flake-utils";
  };

  outputs = { self, nixpkgs, flake-utils }:
    flake-utils.lib.eachDefaultSystem (system:
      let
        pkgs = import nixpkgs {
          inherit system;
          config = {
            android_sdk.accept_license = true;
            allowUnfree = true;
          };
        };

        androidComposition = pkgs.androidenv.composeAndroidPackages {
          buildToolsVersions = [ "34.0.0" ];
          platformVersions = [ "34" ];
          abiVersions = [ "armeabi-v7a" "arm64-v8a" "x86" "x86_64" ];
          includeNDK = true;
          ndkVersions = [ "26.3.11579264" ];
        };

        androidSdk = androidComposition.androidsdk;
        jdk = pkgs.openjdk17;
      in
      {
        devShells.default = pkgs.mkShell {
          name = "openboard-dev-shell";

          buildInputs = [
            jdk
            androidSdk
            pkgs.git
          ];

          ANDROID_HOME = "${androidSdk}/libexec/android-sdk";
          ANDROID_SDK_ROOT = "${androidSdk}/libexec/android-sdk";
          JAVA_HOME = "${jdk.home}";
          GRADLE_OPTS = "-Dorg.gradle.project.android.aapt2FromMavenOverride=${androidSdk}/libexec/android-sdk/build-tools/34.0.0/aapt2";

          shellHook = ''
            export ANDROID_HOME="${androidSdk}/libexec/android-sdk"
            export ANDROID_SDK_ROOT="${androidSdk}/libexec/android-sdk"
            export ANDROID_NDK_ROOT="${androidSdk}/libexec/android-sdk/ndk/26.3.11579264"
            export NDK_HOME="${androidSdk}/libexec/android-sdk/ndk/26.3.11579264"
            export JAVA_HOME="${jdk.home}"
            export GRADLE_USER_HOME="$HOME/.cache/gradle"
            export ANDROID_USER_HOME="$HOME/.cache/android"
            export GRADLE_OPTS="-Dorg.gradle.project.android.aapt2FromMavenOverride=$ANDROID_HOME/build-tools/34.0.0/aapt2"
            export PATH="$ANDROID_HOME/platform-tools:$PATH"
            echo "OpenBoard Android development environment loaded (Java 17, Android SDK 34, NDK 26.3.11579264)"
          '';
        };
      }
    );
}
