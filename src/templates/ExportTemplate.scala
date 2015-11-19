package templates

import model.Note

/**
 * Created by Kenneth on 10/21/2015.
 */
object ExportTemplate {
  def fill(notes : Seq[Note]) : String =
    """<?xml version="1.0" encoding="UTF-8"?>
     <!DOCTYPE en-export SYSTEM "http://xml.evernote.com/pub/evernote-export2.dtd">
    """ +
      <en-export export-date="20151021T044422Z" application="Evernote/Windows" version="5.x">
        {notes.map { NoteTemplate.fill(_) } }
      </en-export>.toString
}
