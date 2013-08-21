/**
 * Add action on nbLayerselect element
 * @param size : max layer number
 */
function addLayerSelectAction(root, method, size){
    $(root + " [data-name=nbLayersselect]").on('change', function(){
        var counter = $(this).val();
        eval(method+"(0, "+counter+", '', '', '')");
        var nbPage = size/counter;
        generatePagination(nbPage);
        $(root+" [data-name=previous]").addClass("active");
    });
}

function addOrderByAction(root, method){
    // OrderBy actions...
    $(root + ' [data-order-by]').click(function() {
        var orderBy   = $(this).data('order-by');
        var counter   = $(root + " [data-name=nbLayersselect]").val();
        var filter    = $(root + " [data-name=searchFilter]").val();
        var direction = $(this).hasClass('descending') ? 'ascending' : 'descending'; // opposite direction

        eval(method+"(0, "+counter+", "+orderBy+", "+filter+", "+direction+")");

        $(this).parents('.nav').find('a').removeClass('ascending descending').
            find('.icon-caret').removeClass('icon-caret-up icon-caret-down').hide();
        if (direction === 'ascending') {
            $(this).addClass('ascending').find('.icon-caret').addClass('icon-caret-up').show();
        } else {
            $(this).addClass('descending').find('.icon-caret').addClass('icon-caret-down').show();
        }
    });
}

function addFilterAction(root, method){
    $(root + " [data-name=searchFilter]").keypress(function(event) {
        var counter   = $(root + " [data-name=nbLayersselect]").val();
        var keyCode = (event.keyCode ? event.keyCode : event.which);
        if (keyCode == '13') {
            eval(method+"(0, "+counter+", null, "+$(this).val()+", null)");
            event.stopPropagation();
            event.preventDefault();
        }
    });
}

function addResetFilterAction(root, method){
    // Reset filter action...
    $(root + ' [data-name=resetFilters]').click(function() {
        var counter   = $(root + " [data-name=nbLayersselect]").val();
        $(root + " [data-name=searchFilter]").val('');
        $(root + ' [data-order-by]').removeClass('ascending descending').
            find('.icon-caret').removeClass('icon-caret-up icon-caret-down').hide();
        eval(method+"(0, "+counter+", null, null, null)");
    });
}

/**
 * action call on pagination
 * @param pageSearched : int to know in which page we go
 * @param pageActivated : DOM element we need to activate
 */
function changePage(root, method, pageSearched, pageActivated){

    //get number element on page selected
    var nbByPage = $(root + " [data-name=nbLayersselect]").val();

    //compute first element on next page
    var startElement = (pageSearched-1)*nbByPage;

    //call ajax
    eval(method+"("+startElement+", "+nbByPage+", null, null, null)");

    //remove all style active on pagination
    $(root + " [data-name=paging] > .active").removeClass("active");

    //activate selected page
    $(pageActivated).addClass("active")

    //if it's last page, next paging is disabled
    if(pageSearched==$(root + " [data-name=paging]> .page").length){
        $(root + "[data-name=next]").addClass("active");
    }

    //if it's first page, previous paging is disabled
    if(pageSearched==1){
        $(root + "[data-name=previous]").addClass("active");
    }
}

/**
 * Generate pagination part when element number is change
 * @param nbPage : element number by page
 */
function generatePagination(root, method, nbPage){
    //clear paging
    $(root + " [data-name=paging]").empty();

    //if they have only one page, we don't show paging
    if(nbPage>1){

        //add previous button
        $(root + " [data-name=paging]").append('<li data-name="previous"><a href="#" class="text-info">&laquo;</a></li>');

        //iterate to add require pages. Activate first page by default
        for(var i=0; i<nbPage; i++){
            var page = i+1;
            if(page==1){
                $(root + " [data-name=paging]").append('<li class="active page"><a href="#" class="text-info">'+page+'</a></li>');
            }
            else{
                $(root + " [data-name=paging]").append('<li class="page"><a href="#" class="text-info">'+page+'</a></li>');
            }
        }

        //add next button
        $(root + " [data-name=paging]").append('<li data-name="next"><a href="#" class="text-info" >&raquo;</a></li>');

        //add action on previous button
        $(root + "[data-name=previous]").on('click', function(){
            if(!$(this).hasClass("active")){
                var currentPage = $(root + " [data-name=paging] > .page.active").text();
                var pageSearched = new Number(currentPage)-1;
                changePage(root, method, pageSearched, $(root + " [data-name=paging] > .page").get(pageSearched-1));
            }
        });

        //add action on next button
        $(root + "[data-name=next]").on('click', function(){
            if(!$(this).hasClass("active")){
                var currentPage = $(root + " [data-name=paging] > .page.active").text();
                var pageSearched = new Number(currentPage)+1;
                changePage(root, method, pageSearched, $(root + " [data-name=paging] > .page").get(pageSearched-1));
            }
        });

        //add action on pages
        $(root + " [data-name=paging] > .page").on('click', function(){
            if(!$(this).hasClass("active")){
                var pageSearched = $(this).text();
                changePage(root, method, pageSearched, this);
            }
        });
    }
}