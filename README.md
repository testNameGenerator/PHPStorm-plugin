**Shortcut:**
Ctrl + Shift + U

**Features:**
- Supports multiple cursors
- Can insert test methods based on the current cursor(s) lines contents

**Limitations / bugs:**
- when editor not fully loaded and shortcut triggered, may cause Exception
- will only add test names, does not update existing ones by editing the original text in the method's doc block comment.
- doesn't use the editor's tab / spaces settings, uses "\t" by default
- doesn't Undo
- works only in Intellij IDEA Community Edition
