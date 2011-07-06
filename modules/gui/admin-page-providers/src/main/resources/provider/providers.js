
function matchesKeyword(val,usedValues){
        
    var str = usedValues;
    var array = new Array();
    array = str.split(",");
    for(i=0; i<array.length; i++){
        candidate = array[i];
        if(val == candidate){
            //TODO make the input text color red and block the add button
            alert("Id already exist : " + str);
            break;
        }
    }
    
}


