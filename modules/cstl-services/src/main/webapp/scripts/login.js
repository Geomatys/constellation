$(function(){
     document.getElementById("msg-error").style.visibility="hidden";
		if(document.location.href.split("?")[1]=="failed"){				
			document.getElementById("msg-error").style.visibility="visible";
		}
		else
			document.getElementById("msg-error").style.visibility="hidden";
    });