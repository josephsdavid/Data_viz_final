let
  pkgs = import <nixpkgs> {};
in
  pkgs.mkShell {
    name = "viz";
    buildInputs = with pkgs; [
      zip
      jdk
      processing
      eclipses.eclipse-java
    ];
    shellHook = ''
      export SOURCE_DATE_EPOCH=$(date +%s) 
      '';

  }
