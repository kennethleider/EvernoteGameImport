package templates

import java.time.format.{FormatStyle, DateTimeFormatter}

import model.{Note, GameDetails}

import scala.xml.{Unparsed, Node}

object ContentTemplate {
Node
  def fill(note: Note) = {
    <en-note>
      <br/>
      <div style="font-size: 16px">
        <div>
          <div style="color:rgb(198, 212, 223);font-family:Arial, Helvetica, sans-serif;font-size:12px;background:rgb(27, 40, 56);">
            <div style="background-position:50% 0%;background-repeat:no-repeat;background-image:url(&apos;http://store.steampowered.com/app/57800&amp;apos;http://cdn.akamai.steamstatic.com/steam/apps/57800/page_bg_generated_v6.jpg?t=1395368240&amp;apos;&apos;);">
              <div>
                <div>
                  <div style="padding:0px;margin:0px auto;width:940px;">
                    <div style="padding:0px;margin:0px;padding-top:0px;">
                      <div style="padding:0px;margin:0px auto;position:relative;text-align:left;padding-top:0px;height:46px;background-image:none;background-repeat:repeat-x;">
                        <div style="padding:0px;margin:0px;color:white;font-family:&apos;Motiva Sans&apos;, Arial, Helvetica, sans-serif;font-size:26px;float:left;max-width:700px;padding-left:0px;">
                          {note.title}
                        </div>
                        <div style="padding:0px;margin:0px;clear:both;"></div>
                      </div>
                    </div>
                  </div>
                  <div style="padding:0px;margin:0px;clear:left;"></div>
                  <div style="padding:0px;margin:0px;">
                    <div style="padding:0px;margin:0px auto;padding-bottom:1px;background-position:50% 100%;background-repeat:no-repeat;">
                      <div style="padding:0px;margin:0px auto;width:940px;height:350px;background:linear-gradient(to right, rgba(0, 0, 0, 0) 50%, rgba(0, 0, 0, 0.4) 100%);">
                        <div style="padding:0px;margin:0px;width:616px;float:left;">
                          <div style="padding:0px;margin:0px;">
                            <div style="padding:0px;margin:0px;margin-bottom:7px;">
                              <img class="width: 324px;height: 151px;" src={note.imageURL}/>
                            </div>
                            <div style="padding:0px;margin:0px;max-height:108px;overflow:hidden;font-size:13px;line-height:18px;padding-right:16px;">
                              {note.description}
                            </div>
                            <div style="padding:0px;margin:0px;cursor:pointer;">
                              <div style="padding:0px;margin:0px;font-size:12px;color:rgb(97, 104, 109);margin-top:10px;">
                                User reviews:
                                <span style={reviewStyle(note.review.tone)}>
                                  {note.review.description}
                                </span>
                                ({note.review.count} reviews)
                              </div>
                            </div>

                            <div style="padding:0px;margin:0px;font-size:12px;color:rgb(97, 104, 109);margin-top:10px;">
                              Release Date:
                              <span style="padding:0px;margin:0px;color:rgb(143, 152, 160);">{note.releaseDate.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM))}</span>
                            </div>

                            <div style="padding:0px;margin:10px 0px;font-size:11px;">
                              <div style="padding:0px;margin:0px;line-height:19px;font-size:12px;color:rgb(97, 104, 109);">
                                Popular user-defined tags for this product:
                              </div>
                              <div style="padding:0px;margin:0px;white-space:nowrap;height:22px;">
                                {note.steamTags.map { tag =>
                                <a href={tag.url}
                                   style="padding:0px 7px;margin:0px;text-decoration:none;color:rgb(103, 193, 245);display:inline-block;line-height:19px;margin-right:2px;border-radius:2px;box-shadow:none;cursor:pointer;margin-bottom:3px;max-width:200px;white-space:nowrap;overflow:hidden;text-overflow:ellipsis;background-color:rgba(103, 193, 245, 0.2);">
                                  {tag.desc}
                                </a>
                              }}
                              </div>
                            </div>
                          </div>
                        </div>
                        <div style="padding:0px;margin:0px;width:324px;margin-left:0px;float:right;">
                          <div style="padding:0px;margin:0px;">
                            <div style="padding:0px;margin:0px;color:rgb(143, 152, 160);line-height:20px;">
                              <div style="padding:0px;margin:0px;">
                                <div style="padding:0px;margin:0px;">
                                  <div style="padding:0px;margin:0px;">

                                    <b style="padding:0px;margin:0px;color:rgb(97, 104, 109);font-weight:normal;">
                                      Genre:
                                    </b>{note.genres.map { genre =>
                                    <a href={genre.url}
                                       style="padding:0px;margin:0px;text-decoration:none;color:rgb(255, 255, 255);">
                                      {genre.desc}</a>
                                  }}
                                    <br style="padding:0px;margin:0px;"/>

                                    <b style="padding:0px;margin:0px;color:rgb(97, 104, 109);font-weight:normal;">
                                      Developer:
                                    </b>

                                    <a href={note.developer.url}
                                       style="padding:0px;margin:0px;text-decoration:none;color:rgb(255, 255, 255);">
                                      {note.developer.desc}
                                    </a>

                                    <br style="padding:0px;margin:0px;"/>

                                    <b style="padding:0px;margin:0px;color:rgb(97, 104, 109);font-weight:normal;">
                                      Publisher:
                                    </b>

                                    <a href={note.publisher.url}
                                       style="padding:0px;margin:0px;text-decoration:none;color:rgb(255, 255, 255);">
                                      {note.publisher.desc}
                                    </a>

                                    <br style="padding:0px;margin:0px;"/>


                                    <b style="padding:0px;margin:0px;color:rgb(97, 104, 109);font-weight:normal;">
                                      Play Time:
                                    </b>
                                    {"%.2f".format(note.playTime)} Hours
                                    <br style="padding:0px;margin:0px;"/>

                                  </div>


                                  <div style="padding:0px;margin:0px;">
                                    <br style="padding:0px;margin:0px;"/>

                                    <a href={note.website} target="_blank"
                                       style="padding-left:8px;padding:0px;margin:0px;text-decoration:none;color:rgb(103, 193, 245);height:22px;padding-top:4px;line-height:17px;border-radius:1px;display:block;margin-bottom:2px;background:rgba(103, 193, 245, 0.0980392);">
                                      Visit the website
                                    </a>


                                    <a href={note.updateHistory}
                                       style="padding-left:8px;padding:0px;margin:0px;text-decoration:none;color:rgb(103, 193, 245);height:22px;padding-top:4px;line-height:17px;border-radius:1px;display:block;margin-bottom:2px;background:rgba(103, 193, 245, 0.0980392);">
                                      View update history
                                    </a>
                                    <a href={note.news}
                                       style="padding-left:8px;padding:0px;margin:0px;text-decoration:none;color:rgb(103, 193, 245);height:22px;padding-top:4px;line-height:17px;border-radius:1px;display:block;margin-bottom:2px;background:rgba(103, 193, 245, 0.0980392);">
                                      Read related news
                                    </a>

                                    <a href={note.discussions}
                                       style="padding-left:8px;padding:0px;margin:0px;text-decoration:none;color:rgb(103, 193, 245);height:22px;padding-top:4px;line-height:17px;border-radius:1px;display:block;margin-bottom:2px;background:rgba(103, 193, 245, 0.0980392);">
                                      View discussions
                                    </a>

                                    <a href={note.groupSearch}
                                       style="padding-left:8px;padding:0px;margin:0px;text-decoration:none;color:rgb(103, 193, 245);height:22px;padding-top:4px;line-height:17px;border-radius:1px;display:block;margin-bottom:2px;background:rgba(103, 193, 245, 0.0980392);">
                                      Find Community Groups
                                    </a>
                                  </div>
                                </div>
                              </div>

                            </div>
                          </div>

                        </div>

                      </div>
                      <div style="clear:both;padding:0px;margin:0px auto;margin-top:20px;width:940px;background:linear-gradient(to right, rgba(0, 0, 0, 0) 50%, rgba(0, 0, 0, 0.4) 100%);">
                        <div style="padding:0px;margin:0px;">
                          <br/>
                          <h2 style="text-align:left;color:rgb(103, 193, 245);padding:0px;margin:0px;line-height:26px;margin-bottom:1px;background-image:url(http://store.akamai.steamstatic.com/public/images/v6/maincol_gradient_rule.png);background-position:0% 100%;background-repeat:no-repeat;font-weight:normal;font-size:18px;font-family:'Motiva Sans Light', 'Motiva Sans', arial, tahoma, sans-serif;">
                            About This Game
                          </h2>
                          {Unparsed(note.longDescription)}
                        </div>
                      </div>
                      <div style="clear:both;padding:0px;margin:0px auto;margin-top:20px;width:940px;background:linear-gradient(to right, rgba(0, 0, 0, 0) 50%, rgba(0, 0, 0, 0.4) 100%);">
                        <div style="padding:0px;margin:0px;">
                          <br/>
                          <h2 style="text-align:left;color:rgb(103, 193, 245);padding:0px;margin:0px;line-height:26px;margin-bottom:1px;background-image:url(http://store.akamai.steamstatic.com/public/images/v6/maincol_gradient_rule.png);background-position:0% 100%;background-repeat:no-repeat;font-weight:normal;font-size:18px;font-family:'Motiva Sans Light', 'Motiva Sans', arial, tahoma, sans-serif;">
                            Owned Downloadable Content
                          </h2>
                          <div style="padding:0px;margin:0px;">
                            <br style="padding:0px;margin:0px;"/>
                            {note.dlc.map { node =>
                            <a href={node.url} target="_blank"
                               style="padding-left:8px;padding:0px;margin:0px;text-decoration:none;color:rgb(103, 193, 245);height:22px;padding-top:4px;line-height:17px;border-radius:1px;display:block;margin-bottom:2px;background:rgba(103, 193, 245, 0.0980392);">
                              {node.desc}
                            </a>
                          }}
                          </div>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </en-note>
  }

  def reviewStyle(reviewType: String) =
    reviewType match {
      case "positive" => "padding:0px;margin:0px;color:#66C0F4;"
      case _ => "padding:0px;margin:0px;color:#A34C25;"
    }
}
