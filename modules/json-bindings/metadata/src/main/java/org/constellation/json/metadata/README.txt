The NumerotedPath and TemplateApplicator classes were parts of an attempt to avoid duplication in the JSON output file
when the template have two or more nodes with the same path. For example we may have:


    {
        "block":{
            "multiplicity":1,
            "path":null
            "content":[{
                "field":{
                    "multiplicity":1,
                    "path":"identificationInfo.descriptiveKeywords.keyword"
                }
            },{
                ...etc...
            },{
                "field":{
                    "multiplicity":1,
                    "path":"identificationInfo.topicCategory"
                }
            }
        ]}
    },{
        "block":{
            "multiplicity":60,
            "path":"identificationInfo.descriptiveKeywords",
            "content":[{
                "field":{
                    "multiplicity":60,
                    "path":"identificationInfo.descriptiveKeywords.keyword"
                }
            },{
                ...etc...
            }
        ]}
    }


In the above example, the same path ("identificationInfo.descriptiveKeywords.keyword") is repeated twice.
The first block will show only the first occurrence (because of "multiplicity":1) while the second block
will show all occurrences.  It may be desirable to omit from the second block all elements already shown
in the first block. However this objective raises some tricky issues:

1) Shall we omit only the first "keyword", or the first "descriptiveKeywords" (thus loosing any keywords
   after the first one in the first "descriptiveKeywords"), or the first "identificationInfo" instance?
   Omitting only the first "keyword" would probably be confusing for the user. Omitting the first block
   "descriptiveKeyword" may be closer to our intend, but there is nothing in the above template telling
   us that. This is because the first block contains an element ("topicCategory") which is normally not
   part of descriptive keywords, so that block is a mix of information from different places.

2) Omitting elements requires that we take trace of remaining elements after we have show some of them.
   We can do that with the TemplateApplication.remainingValues hash map. This map requires distinct keys
   for the same path applied on different instances of a metadata value. For example the two following
   paths are distincts:

      - identificationInfo[0].descriptiveKeywords[0].keyword
      - identificationInfo[0].descriptiveKeywords[1].keyword

   This is handled by the NumerotedPath class. When an element has been shown, we need to check if the
   parent element became empty. This can be handled by a 'prune' operation applied after we created the
   tree (in comparison, in the simpler version it is possible to prune on-the-fly, without a need for a
   post-operation). Consequently handling of such special cases make the code much more complex.


For now, we abandoned the attempt to omit the information already shown. But the classes written during
our experiment are still present in this directory with the .bak extension.
