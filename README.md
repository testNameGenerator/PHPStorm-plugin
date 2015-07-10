**Shortcut:**
Ctrl + Alt + U, U

**Features:**
- Supports multiple cursors
- Can insert test methods based on the current cursor(s) lines contents. Will update method if shortcut is triggered from inside a docblock comment that's followed by a "public function test______" string
- Uses the project's indent settings; defaults on four spaces.

**Limitations / bugs:**
- no PHP-only file filtering was set, will trigger and behave the same for any file

**Features under way:**
- setup multiple tests sets (unit, integration), to be used by the file generator and configurable in each
- display editor gutter icon with link to the method's test file(s)
- create test file for a method or for a class

**Install:**
Open PhpStorm's File / Settings... menu, go to Plugins and click the "Install plugin from disk..." button.
Search for the jar file (download from the [Releases page](https://github.com/testNameGenerator/PHPStorm-plugin/releases)), click OK and restart the editor.

Check out the [SublimeText](https://github.com/testNameGenerator/SublimeText-plugin) or [Eclipse](https://github.com/testNameGenerator/Eclipse-plugin) versions of the plugin.
Feel free to [submit bugs or feature requests](https://github.com/testNameGenerator/PHPStorm-plugin/issues).
