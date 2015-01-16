**Shortcut:**
Ctrl + Shift + U

**Features:**
- Supports multiple cursors
- Can insert test methods based on the current cursor(s) lines contents. Will update method if shortcut is triggered from inside a docblock comment that's followed by a "public function test______" string

**Limitations / bugs:**
- doesn't use the editor's tab / spaces settings, uses "\t" by default
- no PHP-only file filtering was set, will trigger and behave the same for any file

**Install:**
Open PhpStorm's File / Settings... menu, go to Plugins and click the "Install plugin from disk..." button.
Search for the jar file (download from the [Releases page](https://github.com/bogdananton/PhpStorm-testNameGenerator/releases)), click OK and restart the editor.
