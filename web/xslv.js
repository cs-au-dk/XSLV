function cursorPos(text, textarea, lineDumper, lineDumperProperty) {
  var lines = new Array();
  var linecount;
  var col;
  if (textarea.selectionStart) { // IE is too silly to support selectionStart ..
  var head = text.substring(0, textarea.selectionStart);
  lines = head.split("\n");
	linecount = lines.length;
    col = lines[linecount - 1].length;
  if (! linecount) {
    linecount = 1;
    col = 0;
  }
  } else if (document.selection) {
  var range_sel = document.selection.createRange();
  var range_obj = textarea.createTextRange();
  range_obj.moveToBookmark(range_sel.getBookmark());
  range_obj.moveEnd('character', textarea.value.length);
  var pos = textarea.value.length - range_obj.text.length;
  var head = text.substring(0, pos);
  lines = head.split("\n");
  linecount = lines.length;
  col = lines[linecount - 1].length;
  var final = head.substring(head.length - 1, head.length);
  }
  lineDumper[lineDumperProperty] = "Line " + linecount + ", col " + col;
}

function highlight(textarea, line, col, length) {
  var text = textarea.value;
  var lines = text.split("\n");
  var i;
  var linesum = 0;
  for(i=1; i<line; i++) {
    linesum += (lines[i-1].length + 1);
  }
  var start = linesum + col;
  var end = start + length;
  textarea.selectionStart = start;
  textarea.selectionEnd = end;
}
