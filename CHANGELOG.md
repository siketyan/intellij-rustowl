# Changelog

## [0.6.0](https://github.com/siketyan/intellij-rustowl/compare/v0.5.0...v0.6.0) (2026-11-09)


### ⚠ BREAKING CHANGES

* migrate to LspClient API, require 2026.1.4 or later ([#90](https://github.com/siketyan/intellij-rustowl/issues/90))

### Features

* migrate to LspClient API, require 2026.1.4 or later ([#90](https://github.com/siketyan/intellij-rustowl/issues/90)) ([d70a87b](https://github.com/siketyan/intellij-rustowl/commit/d70a87b462c0c8e5cb5b649e71e032c8e86ec925))
* support 2026.3 IDEs ([#89](https://github.com/siketyan/intellij-rustowl/issues/89)) ([dc1f024](https://github.com/siketyan/intellij-rustowl/commit/dc1f02449dc26b5d74fa1c612608f497dd2ef829))
* support RustOwl 0.4.0 ([#87](https://github.com/siketyan/intellij-rustowl/issues/87)) ([542b08c](https://github.com/siketyan/intellij-rustowl/commit/542b08ca64d2cfebca5f09fc796cce2b4cbf6a1a))

## [0.5.0](https://github.com/siketyan/intellij-rustowl/compare/v0.4.1...v0.5.0) (2026-07-19)


### ⚠ BREAKING CHANGES

* Removed support for IDEs earlier than 2025.3.

### Features

* Added support for 2026.2 IDEs.

## [0.4.1](https://github.com/siketyan/intellij-rustowl/compare/v0.4.0...v0.4.1) (2026-01-12)


### Bug Fixes

* Restored the missing dependency for the `com.intellij.modules.platform` module.

## [0.4.0](https://github.com/siketyan/intellij-rustowl/compare/v0.3.3...v0.4.0) (2026-01-12)


### ⚠ BREAKING CHANGES

* Drop support for IDEs older than 2025.2.1.

### Features

* Free IDEs such as IntelliJ IDEA Community Edition are now supported.

## [0.3.3](https://github.com/siketyan/intellij-rustowl/compare/v0.3.2...v0.3.3) (2026-01-12)


### Bug Fixes

* Added support for 2025.3.

## [0.3.2](https://github.com/siketyan/intellij-rustowl/compare/v0.3.1...v0.3.2) (2025-08-05)


### Bug Fixes

* Added support for 2025.2.

## [0.3.1](https://github.com/siketyan/intellij-rustowl/compare/v0.3.0...v0.3.1) (2025-04-10)


### Bug Fixes

* Eliminated some internal API usages.

## [0.3.0](https://github.com/siketyan/intellij-rustowl/compare/v0.2.2...v0.3.0) (2025-04-09)


### ⚠ BREAKING CHANGES

* Old versions of RustOwl (< 0.2.0) is not supported anymore.

### Features

* Added the plugin icon from the RustOwl official logo.
* Support RustOwl v0.2.0.

## [0.2.2](https://github.com/siketyan/intellij-rustowl/compare/v0.2.1...v0.2.2) (2025-03-15)


### Features

* Added support for 2025.1 EAP.

## [0.2.1](https://github.com/siketyan/intellij-rustowl/compare/v0.2.0...v0.2.1) (2025-03-09)


### Bug Fixes

* Fixed the customised colour settings are not used on highlighting.

## [0.2.0](https://github.com/siketyan/intellij-rustowl/compare/v0.1.1...v0.2.0) (2025-03-09)


### Features

* Added settings to specify the path to cargo-owlsp.
* Added settings to customise highlighting colours.

## [0.1.1](https://github.com/siketyan/intellij-rustowl/compare/v0.1.0...v0.1.1) (2025-02-15)


### Bug Fixes

* Fix highlighting is not working when multiple editors opened.
* Fix possibly NullPointerException.

## 0.1.0 (2025-02-09)


### Features

* Initial release.
