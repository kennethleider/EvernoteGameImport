package templates

import model.Note

import scala.xml.Node

object NoteTemplate {
  def fill(note: Note): Node = <note>
    <title>{note.title}</title>
    <content>
      {scala.xml.Unparsed(s"<![CDATA[${ContentTemplate.fill(note)}]]>")}
    </content>
    <created>{note.created.toString().replaceAll("-|:","")}</created>
    <updated>{note.modified.toString().replaceAll("-|:","")}</updated>
    {note.tags.map ( tag => <tag>{tag}</tag> )}
    <note-attributes>
      <source>web.clip</source>
      <source-url>{note.sourceUrl}</source-url>
    </note-attributes>
  </note>

}
