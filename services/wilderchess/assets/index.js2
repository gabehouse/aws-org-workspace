// var ws = new WebSocket("ws://52.35.11.153:80/");
// var ws = new WebSocket("ws://192.168.0.1:8080/");


setupWebSocket();
var searching = false;
var id;
var quickStartEnabled = false;
var passwordRequired = false;

var abilityFont = "Permanent Marker";
var infoBoxTitleFont = "Permanent Marker";
var infoBoxTextFont = "Chelsea Market";
var logButtonFont = "Permanent Marker";
var logTextFont = "Nunito";
var buttonFont = "Permanent Marker";
var portraitFont = "Permanent Marker";
var buttonFont = "Permanent Marker";
var iconFont = "Caesar Dressing";
var tipFont = "Permanent Marker";
var dTextFont = "Chelsea Market";

function setupWebSocket(){
	this.ws = new WebSocket('ws://wilderchess.eba-nsb5rgs7.us-east-2.elasticbeanstalk.com/');
//	this.ws = new WebSocket('ws://wilderchess.eba-nsb5rgs7.us-east-2.elasticbeanstalk.com/');
window.onbeforeunload = function() {
	ws.send("user,disconnect," + id);
};

this.ws.onopen = function() {
	if (!selectInitHappened) {
		ws.send("selectinitrequest");
  }
  if (!passwordRequired) {
    enterCritterPit(',success');
  }
};

this.ws.onmessage = function (evt) {

//alert(evt.data);
    var tokens = evt.data.split(",");
    if (tokens[0] == "startgame") {
    	startGame();
    } else if (tokens[0] == "indicatespots") {

    	if (tokens[1].charAt(0) != 'n') {
        	possibleMovesStr = evt.data;
        }
        showPossibleMoves(evt.data, true);
    } else if (tokens[0] == "move") {
        if (identifyCritter(tokens[1], tokens[2]) != null) {
            move(evt.data);
        }
    } else if (tokens[0] == "moveQueue") {
        displayActionQueue(evt.data);
    } else if (tokens[0] == "indicate") {
        indicateActions(evt.data);
        actionQueueStr = evt.data;
     //   alert(actionQueueStr);
    } else if (tokens[0] == "reset") {
        reset();
    } else if (tokens[0] == "inititems") {
		initItems(evt.data);
    } else if (tokens[0] == "energy" || tokens[0] == "health") {
        changeBars(evt.data);
    } else if (tokens[0] == "calculating") {
    	if (tokens[1] == "begin") {
    		calculateTurn(evt.data);
    	} else {
    		calculateTurn(evt.data);
    	}
    }  else if (tokens[0] == "clickable" || tokens[0] == "notclickable") {
        displayAbilities(evt.data);
    } else if (tokens[0] == "removeabilities") {
    	displayAbiltiies("remove");
    } else if (tokens[0] == "animate") {
    	animate(evt.data);
    } else if (tokens[0] == "effecttt") {
    	createEffectTT(evt.data);
    } else if (tokens[0] == "itemdisplay") {
    	otherItemDisplay(evt.data);
    } else if (tokens[0] == "showgrid") {
		showGrid(evt.data);
    } else if (tokens[0] == "critterselect") {
    	critterSelect(evt.data);
    } else if (tokens[0] == "initialphaseend") {
    	initialPhaseEnd(evt.data);
    } else if (tokens[0] == "dead") {
    	death(evt.data);
    } else if (tokens[0] == "combatlog") {
    	combatLog(evt.data);
    } else if (tokens[0] == "chat") {
    	updateChatLog(evt.data);
    } else if (tokens[0] == "actionlog") {
    	updateActionLog(evt.data);
    } else if (tokens[0] == "morale") {
    	moraleBar(evt.data);
    } else if (tokens[0] == "revive") {
    	revive(evt.data);
    } else if (tokens[0] == "option"){
    	actionOptions(evt.data);
    } else if (tokens[0] == "possiblemoves") {
    	possibleMovesStr = tokens[1];
    } else if (tokens[0] == "newcritter") {
        showPossibleMoves(",no moves", false);
        possibleMovesStr = "";
    } else if (tokens[0] == "transition") {
    	transition();
    } else if (tokens[0] == "critterupdate") {
    	critterUpdate(evt.data);
    } else if (tokens[0] == "gameover") {
    	gameOver(evt.data);
    } else if (tokens[0] == "setid") {
    	id = tokens[1];
    } else if (tokens[0] == "selectinit") {
    	selectInit(evt.data);
    } else if (tokens[0] == "entercritterpit") {
    	enterCritterPit(evt.data);
    }
};

this.ws.onclose = function() {

	setTimeout(setupWebSocket, 50);
};

this.ws.onerror = function(err) {
	setTimeout(setupWebSocket, 50);
//    alert("Error: " + err);
};
}

function searchForOpponent() {
	if (searching) {
		searchCancel();
		battleAiButton.enable();
	} else {
		ws.send("searchqueue,add");
		//searchButton.disable();
		searching = true;
		searchButton.getText().attr('text', 'Searching...').attr("font-size", 18);
		battleAiButton.disable();
	}
}

function playBot() {
	ws.send("playbot");
}


 Raphael.fn.print_center = function(x, y, string, font, size, letter_spacing) {
    var path = this.print(x, y, string, font, size, 'baseline', letter_spacing);
    var bb = path.getBBox();

    var dx = (x - bb.x) - bb.width / 2;
    var dy = (y - bb.y) - bb.height / 2;

    return path.transform("t" + dx + "," + dy);
}


var totalCritters = 12;
var totalCritterNames = ["wolf", "fox", "lion", "turtle", "bull", "dove", "donkey", "newt", "bat",  "hawk", "heron", "pig"];
var totalCritterSelectPaths = ["assets/wolf/fullwolf.png","assets/fox/fullfox.png","assets/lion/fulllion.png","assets/turtle/fullturtle.png","assets/bull/fullbull.png","assets/dove/fulldove.png","assets/donkey/fulldonkey.png","assets/newt/fullnewt.png","assets/bat/fullbat.png","assets/hawk/fullhawk.png","assets/heron/fullheron.png","assets/pig/fullpig.png"];
var totalItemPaths = ["firstaidkit.png", "warhorn.png", "oilbomb.png", "jestershat.png", "thiefgloves.png", "smokebomb.png"];
var totalItemNames = ["Crimberry Pack", "War Horn", "Oil Bomb", "Ratland Jester's Hat", "Thief Gloves", "Smoke Bomb"];
var WIDTH = 1080;
var HEIGHT = 675;
var paperX = window.screen.availWidth/2-540;
var paperY = window.screen.availHeight/2-338;
var blockColor = "#b3e9ea";
var attackColor = "#f0bfcd";
var supportColor = "#fffccb";
var moveColor = "#e0eea7";
var GAMEOVER = false;
//----------------------------------------------------------------------------------------------------------------------------------------------------------
var backgroundPaper = Raphael(window.screen.availWidth/2-550,window.screen.availHeight/2-348,1100,695);

var edge = backgroundPaper.rect(5,5,1090,685).attr('fill', 'black').glow({'width':8});
var gridPaper = Raphael(window.screen.availWidth/2-540,window.screen.availHeight/2-338,1080,675);


var critterSelectPaper = Raphael(window.screen.availWidth/2-540,window.screen.availHeight/2-338,WIDTH,HEIGHT);
//var titleScreen = critterSelectPaper.image("assets/backgrounds/AmTitleScreen.jpg",0,0,1,1);
var titleScreen = critterSelectPaper.image("assets/backgrounds/AmTitleScreen.png",-46,0,0,0);
var lockedTitleScreen = critterSelectPaper.image("assets/backgrounds/lockedtitlescreen.png",0,0,WIDTH,HEIGHT);
titleScreen.attr({'width':1172, 'height':HEIGHT});

var enterRect = critterSelectPaper.rect(WIDTH/2-130, HEIGHT/2 - 50, 260,50).attr({'fill':'yellow', 'stroke-width':3});
var enterText = critterSelectPaper.text(WIDTH/2, HEIGHT/2 - 25, "Enter Wilderchess").attr({"font-size": 25, "font-family": buttonFont});
var enterClickRect = critterSelectPaper.rect(WIDTH/2-130, HEIGHT/2 - 50, 260,50).attr({'stroke-opacity':0, 'opacity':0, 'fill':'black'});
var enterRects = critterSelectPaper.set(enterRect, enterClickRect);
//enterRect.glow = enterRect.glow({'color':'white', 'width':10, 'opacity': 0.7, 'offsetx':15, 'offsety':7});
var enterPassword = document.getElementById('enter');
var block = document.getElementById('block');
enterPassword.style.left = (paperX + WIDTH/2 - 95) + "px";
enterPassword.style.top = (paperY + HEIGHT/2) + "px";
enterPassword.style.height = "30px";
document.getElementById('password').style.width = "180px";
enterPassword.style.visibility = "visible";
enterText.node.setAttribute("class","donthighlight");
block.style.left = "0px";
block.style.top = "0px";
block.style.height = screen.height + "px";
block.style.width = screen.width + "px";
block.style.visibility = "hidden";
var textField = document.getElementById('password');

document.getElementById('submitpassword').style.visibility = "hidden";
textField.style.visibility = "hidden";




function enterCritterPit(str) {
	var tokens = str.split(',');
	if (tokens[1] == "success") {
		textField.parentNode.removeChild(textField);
		block.style.visibility = "hidden";
		lockedTitleScreen.animate({'opacity':0},1500, function() {
			lockedTitleScreen.remove();
		});
		critterSelectElements.attr('opacity',0).show();
		critterSelectElements.animate({'opacity':1},1500);
		selectKnob.animate({'opacity':0.5},1500);
		scrollKnob.show();
		document.getElementById('bugslide').style.visibility = "visible";
		document.getElementById('slideout').style.visibility = "visible";
		enterRect.remove();
		enterClickRect.remove();
		enterText.remove();



	} else {
		block.style.visibility = "hidden";
		document.getElementById('password').style.visibility = "hidden";
		document.getElementById('password').blur();
		document.getElementById('password').value = "";
		enterRect.animate({'height':50},500);
		enterText.animate({'opacity':0}, 250, function(){
		enterText.attr('text', 'Enter Wilderchess'); enterText.animate({'opacity':1}, 250);
	});
	enterRects.animate({'height':50},500, function(){});
	}
}

function authenticate(msg) {
	ws.send('entercritterpit,' + msg);

}

function blockClick() {
	document.getElementById('password').focus();
}

enterClickRect.hover(
function(){
	var isChromium = window.chrome,
    winNav = window.navigator,
    vendorName = winNav.vendor,
    isOpera = winNav.userAgent.indexOf("OPR") > -1,
    isIEedge = winNav.userAgent.indexOf("Edge") > -1,
    isIOSChrome = winNav.userAgent.match("CriOS");

	if(isIOSChrome){
   // is Google Chrome on IOS
	} else if(isChromium !== null && isChromium !== undefined && vendorName === "Google Inc." && isOpera == false && isIEedge == false) {
   		block.style.visibility = "visible";
		enterText.animate({'opacity':0}, 250, function(){
			enterText.animate({'opacity':1}, 250);
			enterText.attr('text', 'Password?');
		});
		enterRects.animate({'height':100},500, function(){
			document.getElementById('password').style.visibility = "visible";
			document.getElementById('password').focus();
		});
	} else {
		enterText.animate({'opacity':0}, 250, function(){
			enterText.animate({'opacity':1}, 250);
			enterText.attr('text', 'Chrome Only');
		});

	}



},
function(){



});

var critterSelectElements = critterSelectPaper.set();
var itemRectX = WIDTH/2-140;
var itemRectY = 310;
var itemRectWidth = 280;
var itemRectHeight = 150;
var crittersSelected = [];
var selectedCritters = [];
var itemsSelected = [];
var selectedItems = [];
var critterSelectables = [];
var itemSelectables = [];
 var itemHoverRect = critterSelectPaper.rect(0, 570, WIDTH, HEIGHT - 570).attr("fill", "#FFFBC4").attr("fill-opacity", 0.5).attr('stroke-opacity','0');
var searchButton = new Button(critterSelectPaper.rect(WIDTH/2 - 155, 10, 150, 40, 6).attr({"fill": "yellow", 'stroke-width': 2}),
							  critterSelectPaper.text(WIDTH/2 - 80, 30, "Vs. Player").attr("font-family", buttonFont).attr("font-size", 18),
							  critterSelectPaper, searchForOpponent);
var battleAiButton = new Button(critterSelectPaper.rect(WIDTH/2 + 5, 10, 150, 40, 6).attr({"fill": "yellow", 'stroke-width': 2}),
							  critterSelectPaper.text(WIDTH/2 + 80, 30, "Vs. AI").attr("font-family", buttonFont).attr("font-size", 18),
							  critterSelectPaper, playBot);

if (quickStartEnabled) {
	lockedTitleScreen.remove();
	enterRect.remove();
	enterText.remove();
	enterClickRect.remove();


} else{
	searchButton.disable();
	searchButton.hide();
	battleAiButton.disable();
	battleAiButton.hide();

}

for (var i = 0; i < totalItemPaths.length; i++) {
	var newItemSelectable = new ItemSelectable("assets/items/" + totalItemPaths[i], i, totalItemNames[i], critterSelectPaper.image("assets/items/" + totalItemPaths[i], WIDTH/2-40, 250, 80,80), critterSelectPaper.image("assets/items/itembox" + (i%3 + 1) + ".png", WIDTH/2-40, 250, 80,80)
																								 , critterSelectPaper.image("assets/items/pressed" + totalItemPaths[i], WIDTH/2-40, 250, 80,80), critterSelectPaper.image("assets/items/background" + totalItemPaths[i], WIDTH/2 - HEIGHT/2-500, -500,  1600, 1600));
	itemSelectables.push(newItemSelectable);

	var miniImg = critterSelectPaper.image("assets/items/mini" + totalItemPaths[i], 700, 10, 35, 35);
	var miniItemSelectable = new MiniItemSelectable(miniImg, totalItemNames[i],  newItemSelectable);
	newItemSelectable.setMiniItemSelectable(miniItemSelectable);
	critterSelectElements.push(newItemSelectable.getPic());
}


itemSelectables.forEach(function(is) {
	is.getPic().click(function() {
	searchButton.disable();
	battleAiButton.disable();
	if (!(searchButton.getRect()[1].attr('text') == 'searching...')){
		if (!is.getSelected()) {
			if (itemsSelected.length < 3) {
				is.setSelected(true);

				itemsSelected.push(is.getName());
				selectedItems.push(is);

				is.getPic().animate({transform : 's0'},100, function(){
					is.getPic().hide();

				});
				is.getMiniItemSelectable().show();

				var onleft = true;
				for (var i = 0; i < itemSelectables.length; i++) {
					if (itemSelectables[i] == is) {
						onleft = false;
					} else if (onleft) {
						itemSelectables[i].setX(itemSelectables[i].getX() + 50);
						itemSelectables[i].getPic().animate({x:itemSelectables[i].getX()}, 200, ">");
					} else {
						itemSelectables[i].setX(itemSelectables[i].getX() - 50);
						itemSelectables[i].getPic().animate({x:itemSelectables[i].getX()}, 200, ">");
					}
				}

			}
			if (itemsSelected.length == 3 && crittersSelected.length == 4 && !searching) {
				searchButton.enable();
				battleAiButton.enable();
			}
		}
	}
	});
});



var cSHelpText = critterSelectPaper.text(WIDTH/2, 210, "Select four fighters").attr({"font-size": 20, "font-family":tipFont});
var iSHelpText = critterSelectPaper.text(WIDTH/2, 550, "...and three items").attr({"font-size": 20, "font-family":tipFont});
var critterSelectHelpText = gridPaper.set(cSHelpText, iSHelpText);
cSHelpText.node.setAttribute("class","donthighlight");
iSHelpText.node.setAttribute("class","donthighlight");


var topAbilRectY = 197;
var leftAbilRectX = 75;
var rightAbilRectX = (WIDTH - 150)/2 + 255;
var abilRectHeight = 184;
var botAbilRectY = topAbilRectY + abilRectHeight;
var abilRectWidth = (WIDTH - 150)/2 - 180;
var bAbilRectWidth = leftAbilRectX + abilRectWidth;
var bAbilRectHeight = 40;
var bAbilRectY = botAbilRectY + abilRectHeight - bAbilRectHeight;
var bAbilRectX = abilRectWidth + leftAbilRectX;

var abil1Rect = new InfoBox (leftAbilRectX, topAbilRectY, abilRectWidth, abilRectHeight, critterSelectPaper,"", 5,13);
var abil2Rect = new InfoBox(rightAbilRectX, topAbilRectY, abilRectWidth, abilRectHeight, critterSelectPaper, "", 5,13);
var abil3Rect = new InfoBox(leftAbilRectX, botAbilRectY, abilRectWidth, abilRectHeight, critterSelectPaper, "", 5,13);
var abil4Rect = new InfoBox(rightAbilRectX, botAbilRectY, abilRectWidth, abilRectHeight, critterSelectPaper, "", 5,13);
var bpRect = new InfoBox(abilRectWidth + leftAbilRectX, bAbilRectY, bAbilRectWidth, bAbilRectHeight, critterSelectPaper, "", 5,13);
var abilRects = [abil1Rect,abil2Rect,abil3Rect,abil4Rect,bpRect];

abilRects.forEach(function(ar) {
	ar.getRect().attr('stroke-width', 0.5)
	ar.setFontSize(14);
});

var itemRect = new InfoBox(itemRectX, itemRectY, itemRectWidth, itemRectHeight, critterSelectPaper,"",30, 17);
itemRect.getRect().hide().attr('stroke','#704000');
itemRect.setColor('#FDECB4');
itemRect.setOpacity(0.8);


abilRects.forEach(function(a) {
	a.setOpacity(0.85);
	a.getRect().hide();
	a.getRect().attr({"stroke": "#704000", "stroke-width": 1.5});
});
var visibleSelectableCapacity = 6;
var selectableAreaWidth = 6 * 115;
var critterScrollPaper = Raphael(paperX + WIDTH/2 - 115 * 3,60 + paperY, selectableAreaWidth,130);
//var critterScrollPaperframe = critterScrollPaper.rect(0,0, selectableAreaWidth,120).attr({'fill': 'white', 'opacity': 0, 'stroke-opacity': 0});

var miniCritterSelectables = critterSelectPaper.set();

function MiniItemSelectable(selectable, name, parentSelectable) {
	this.selectable = selectable;
	this.parentSelectable = parentSelectable;
	this.getParentSelectable = function() {return this.parentSelectable}
	this.getSelectable = function(){return this.selectable}
	this.name = name;
	this.getName = function(){return this.name;}
	this.selectable.animate({transform : 's0'},100);
	this.x = 750;
	this.setX = function(x) {this.x = x};
	this.getX = function() {return this.x};
	var ps = this.parentSelectable;
	var ms = this.selectable;
	var n = this.name;
	this.selectable.click(function(){

		ms.animate({transform : 's0'},100, function(){
			this.selectable.hide();
		});

		var onleft = false;
		for (var i = 0; i < selectedItems.length; i++) {
			if (selectedItems[i].getName() == n) {
				onleft = true;
			} else if (onleft) {
				var lMS = selectedItems[i].getMiniItemSelectable();
				lMS.setX(lMS.getX() + 35);
				lMS.getSelectable().animate({x: lMS.getX()}, 700, 'bounce');
			}
		}

		ps.setSelected(false);
		var index = itemsSelected.indexOf(n);
		if (searching){
			searchCancel();
		}

		searchButton.disable();
		battleAiButton.disable();
		selectedItems.splice(index,1);
		itemsSelected.splice(index,1);


		ps.getPic().attr('x', ps.getX());

		var notOnRight = true;

		for (var i = 0; i < itemSelectables.length; i++) {

			if (itemSelectables[i] == ps) {
					notOnRight = false;
			} else if (notOnRight) {
				itemSelectables[i].setX(itemSelectables[i].getX() - 50);
				itemSelectables[i].getPic().animate({x:itemSelectables[i].getX()}, 200, ">");
			} else {
				itemSelectables[i].setX(itemSelectables[i].getX() + 50);
				itemSelectables[i].getPic().animate({x:itemSelectables[i].getX()}, 200, ">");
			}
		}

	ps.getPic().transform("s0").show();

	ps.getPic().animate({transform : 's1'},200);
	});

	this.show = function() {
		this.x = 860 - itemsSelected.length * 36;

		this.selectable.attr({x: this.x});
		this.selectable.animate({transform : 's1'},200, function(){
			this.selectable.hide();
		});
	}
	this.hide = function() {this.selectable.hide();}
}


function MiniCritterSelectable(selectable, name, parentSelectable) {
	this.selectable = selectable;
	this.selectable[1].attr('stroke', '#704000');
	this.parentSelectable = parentSelectable;
	this.getParentSelectable = function() {return this.parentSelectable}
	this.getSelectable = function(){return this.selectable}

	this.name = name;
	this.getName = function(){return this.name;}
	this.selectable.animate({transform : 's0'},100);
	this.x = 500;
	this.setX = function(x) {this.x = x};
	this.getX = function() {return this.x};
	var ps = this.parentSelectable;
	var ms = this.selectable;
	var n = this.name;
	this.selectable.click(function(){
		ms[0].animate({transform : 's0'},100);
		ms[1].animate({transform : 's0'},100, function(){
			this.selectable.hide();
		});

		var onright = false;
		for (var i = 0; i < selectedCritters.length; i++) {
			if (selectedCritters[i].getName() == n) {
				onright = true;
			} else if (onright) {

				var rMS = selectedCritters[i].getMiniCritterSelectable();
				rMS.setX(rMS.getX() - 35);
				var img = rMS.getSelectable()[0];
				var circ = rMS.getSelectable()[1];
				img.animate({x: rMS.getX()}, 700, 'bounce');

				circ.animate({cx: rMS.getX()+ 15}, 700, 'bounce');

			}
		}

		var cx = ps.getPortrait()[1].attr('cx');
		var cy = ps.getPortrait()[1].attr('cy');
		var oldKnobX = selectKnob.attr('x');
		var oldKnobW = selectKnob.attr('width');
		var newKnobW = WIDTH / ((critterSelectables.length + 1 - crittersSelected.length) / (critterSelectables.length /2));
		var newpct = oldKnobX / (WIDTH - oldKnobW);
		knobMultiplier = WIDTH / ((critterSelectables.length + 1 - crittersSelected.length) * 115);
		titleScreenMultiplier = 72/(WIDTH - newKnobW) * knobMultiplier;
		var newScrollKnobX = scrollKnob.attr('x') - (newKnobW - oldKnobW) * newpct;
		var newSelectKnobX = selectKnob.attr('x') - (newKnobW - oldKnobW) * newpct;
		scrollXOffset -= 57 - 115*newpct;
		var pctDiff = ps.getPct() - newpct;
		selectKnob.animate({'x': newSelectKnobX, 'width': newKnobW}, 100);
		ps.setSelected(false);
		var index = crittersSelected.indexOf(n);
		if (searching){
			searchCancel();
		}

		searchButton.disable();
		battleAiButton.disable();
		selectedCritters.splice(index,1);
		crittersSelected.splice(index,1);
		var newx = 0;
		var prevcs = critterSelectables[ps.getPos() - 1];
		for (var j = ps.getPos(); j >= 0; j--) {

			if (!ps.getSelected()) {
				newx = ps.getX() + 115 * pctDiff;
			}
		}
		if (newx == 0) {
			newx = selectableAreaWidth /2 - (totalCritters/2) *  115 + 115 * pctDiff;
		}
		ps.setX(newx);
		ps.getPortrait()[0].attr('x', ps.getX() - ps.getWidth()/2);
		ps.getPortrait()[1].attr('cx', newx);


		scrollKnob.attr({'x': newScrollKnobX, 'width': newKnobW});
		var selectKnobBlock = critterSelectPaper.rect(0,selectKnob.attr('y'), WIDTH, selectKnob.attr('height')).attr({'stroke-opacity':0,'fill-opacity':0, 'fill':'yellow'});
		scrollKnob.hide();

		var onleft = true;

		for (var i = 0; i < critterSelectables.length; i++) {

			if (critterSelectables[i] == ps) {
					onleft = false;
			} else if (onleft) {
				critterSelectables[i].setX(critterSelectables[i].getX() - 115 * newpct);
				critterSelectables[i].getPortrait()[0].animate({x:critterSelectables[i].getX() - critterSelectables[i].getWidth()/2}, 200, ">");
				critterSelectables[i].getPortrait()[1].animate({cx:critterSelectables[i].getX()}, 200, ">");
			} else {
				critterSelectables[i].setX(critterSelectables[i].getX() + 115 * (1 -newpct));
				critterSelectables[i].getPortrait()[0].animate({x:critterSelectables[i].getX() - critterSelectables[i].getWidth()/2}, 200, ">");
				critterSelectables[i].getPortrait()[1].animate({cx:critterSelectables[i].getX()}, 200, ">", function(){selectKnobBlock.remove(); scrollKnob.show()});
			}
		}
	ps.getPortrait().transform("s0").show();

//	ps.getPortrait().animate({transform : 's0'},1).show();
	ps.getPortrait().animate({transform : 's1'},200);
	});

	this.show = function() {
		this.x = 150 + crittersSelected.length * 36;

		this.selectable[0].attr({x: this.x});
		this.selectable[1].attr({cx: this.x + 15});
		this.selectable[0].animate({transform : 's1'},200);
		this.selectable[1].animate({transform : 's1'},200, function(){
			this.selectable.hide();
		});
	}
	this.hide = function() {this.selectable.hide();}

}


for (var i = 0; i < totalCritterNames.length; i++) {
	var newCritterSelectable = new CritterSelectable("assets/" + totalCritterNames[i] + "/" + totalCritterNames[i] + "portraitleft.png", i, totalCritterNames[i], critterSelectPaper.image(totalCritterSelectPaths[i],WIDTH/2-200, 150,400,400), critterSelectPaper.image("assets/" + totalCritterNames[i] + "/background" + totalCritterNames[i] + ".png",WIDTH/2 - 800 - HEIGHT/2, -1200,1600,1600).hide());
	critterSelectables.push(newCritterSelectable);
	var miniCritterSelectable = critterSelectPaper.set();
	var miniImg = critterSelectPaper.image("assets/" + totalCritterNames[i] + "/" + totalCritterNames[i] + "miniportraitleft.png", 500, 15, 30, 30);
	var miniCircle = critterSelectPaper.circle(515, 30, 15);
	var miniCritterSelectable = new MiniCritterSelectable(critterSelectPaper.set(miniImg, miniCircle), totalCritterNames[i],  newCritterSelectable);
	newCritterSelectable.setMiniCritterSelectable(miniCritterSelectable);
	if (i > 7) {
		newCritterSelectable.setComingSoon(true);
	}
//	scrollPaperElements.push(newCritterSelectable.getPortrait());
	critterSelectElements.push(newCritterSelectable.getPortrait());
}
// var scrollStrip = critterSelectPaper.rect(0,80,WIDTH,120).attr({'fill': 'white', 'opacity': 0.4, 'stroke-opacity': 0});
critterSelectElements.push(critterSelectables, itemSelectables, critterSelectHelpText, searchButton.getRect(), battleAiButton.getRect());
var knobActiveColor = 'white';
var knobIdleColor = '#FFFBC4';
var knobMultiplier = WIDTH / (critterSelectables.length * 115);
var knobWidth = WIDTH/(critterSelectables.length / 6);
var titleScreenMultiplier = 72/(WIDTH - knobWidth) * knobMultiplier;
var leftArrow = critterSelectPaper.path('M ' + (WIDTH/2 - 115 * 3 - 10) + ',' +  60 + ' L ' + (WIDTH/2 - 115 * 3 - 10) + ',' +  190  + ' L ' + (WIDTH/2 - 115 * 3 - 50) + ',' +  125 + ' L ' + (WIDTH/2 - 115 * 3 - 10) + ',' +  60).attr({'fill': knobIdleColor, 'stroke-opacity': 0, 'opacity':0.8});
var rightArrow = critterSelectPaper.path('M ' + (WIDTH/2 - 115 * 3 + selectableAreaWidth + 10) + ',' +  60 + ' L ' + (WIDTH/2 - 115 * 3 + selectableAreaWidth + 10) + ',' +  190  + ' L ' + (WIDTH/2 - 115 * 3 + 40 + selectableAreaWidth + 10) + ',' +  125 + ' L ' + (WIDTH/2 - 115 * 3 + selectableAreaWidth + 10) + ',' +  60).attr({'fill': knobIdleColor, 'stroke-opacity': 0, 'opacity':0.8});
//var attackRect = critterSelectPaper.rect(0,115,WIDTH/3,20).attr({'fill':attackColor});
//var blockRect = critterSelectPaper.rect(WIDTH/3,115,WIDTH/3,20).attr({'fill':blockColor});
//var supportRect = critterSelectPaper.rect(2*WIDTH/3,115,WIDTH/3,20).attr({'fill':supportColor});
//var typeRects = critterSelectPaper.set(attackRect,blockRect,supportRect);
//typeRects.attr({'opacity':0.8, 'stroke-opacity':0, 'stroke':'#704000'});
var arrows = critterSelectPaper.set(leftArrow, rightArrow);
var blinkIn = function (arrow) {arrow.animate({'opacity':0},1500,function(){blinkOut(arrow)});};
var blinkOut = function (arrow) {arrow.animate({'opacity':0.8},1500,function() {blinkIn(arrow)});};
blinkIn(arrows);
var selectKnob = critterSelectPaper.rect(WIDTH/2 - knobWidth/2,60,knobWidth,130).attr({'fill': '#FFFBC4', 'opacity': 0.5, 'stroke-opacity': 0});
var scrollKnob = critterScrollPaper.rect(selectableAreaWidth/2 - knobWidth/2,0,knobWidth,130).attr({'fill': 'yellow', 'opacity': 0, 'stroke-opacity': 0}).hide();
if (quickStartEnabled) {
scrollKnob.show();
textField.parentNode.removeChild(textField);
}
var scrollPaperElements = critterScrollPaper.set(scrollKnob);
critterSelectElements.push(selectKnob,arrows,itemHoverRect);
if (!quickStartEnabled) {
	critterSelectElements.hide();
}
critterSelectables.forEach(function (cs){
	cs.getPortrait().toFront();
});

var draglx = 0;
var scrollXOffset = 0;
knobs = [selectKnob, scrollKnob];
var knobHovering = false;
var knobClicking = false;


	itemHoverRect.hover(
	function(){
		itemHoverRect.animate({'fill':knobActiveColor}, 350);
		arrows.animate({'fill':knobActiveColor}, 350);
	},
	function(){
		itemHoverRect.animate({'fill':knobIdleColor}, 350);
		arrows.animate({'fill':knobIdleColor}, 350);

	});
knobs.forEach(function(k){
	k.hover(
	function(){
		knobHovering = true;
		selectKnob.animate({'fill':knobActiveColor}, 350);
		arrows.animate({'fill':knobActiveColor}, 350);
		document.body.style.cursor = 'pointer';

	},
	function(){
	knobHovering = false;
	if (!knobClicking) {
		selectKnob.animate({'fill':knobIdleColor}, 350);
		arrows.animate({'fill':knobIdleColor}, 350);
		document.body.style.cursor = 'auto';
	}
	});

	k.drag(
function(dx,dy){
	var cutOffCritterCount = (critterSelectables.length - crittersSelected.length - 6) /2;
	var restricteddx = dx;
	if (dx + scrollXOffset < -cutOffCritterCount * 115) {
		restricteddx = -cutOffCritterCount * 115 - scrollXOffset;
	} else if (dx + scrollXOffset > cutOffCritterCount * 115) {
		restricteddx = cutOffCritterCount * 115 - scrollXOffset;
	}
	critterSelectables.forEach(function (cs){

		cs.getPortrait().transform('T' + -restricteddx + ',' + 0);
	});
	titleScreen.transform('T' + restricteddx * -titleScreenMultiplier + ',' + 0);
	selectKnob.transform('T' + restricteddx * knobMultiplier + ',' + 0);
	scrollKnob.transform('T' + restricteddx * knobMultiplier + ',' + 0);
	draglx = restricteddx;
},
function(){
draglx = 0;
knobClicking = true;
arrows.hide();
},

function(dx,dy){
	var cutOffCritterCount = (critterSelectables.length - crittersSelected.length - 6) /2;
	knobClicking = false;
	if (!knobHovering) {
		selectKnob.animate({'fill':knobIdleColor}, 250);
		arrows.animate({'fill':knobIdleColor}, 250);
	}
	var restricteddx = draglx;
	var firstCritterSelectable;
	var lastCritterSelectable;
	for (var i = 0; i < critterSelectables.length; i++) {
		if (!critterSelectables[i].getSelected()) {
			firstCritterSelectable = critterSelectables[i];
			break;
		}
	}
	for (var i = critterSelectables.length - 1; i >= 0; i--) {
		if (!critterSelectables[i].getSelected()) {
			lastCritterSelectable = critterSelectables[i];
			break;
		}
	}




	selectKnob.transform('T' + (-restricteddx * knobMultiplier) - ',' + 0);
	scrollKnob.transform('T' + (-restricteddx * knobMultiplier) - ',' + 0);
	titleScreen.transform('T' + (-restricteddx * -titleScreenMultiplier) - ',' + 0);
	critterSelectables.forEach(function (cs){
		cs.getPortrait().transform('T' - restricteddx + ',' + 0);
	});

	scrollXOffset += restricteddx;
	critterSelectables.forEach(function(cs){
		var tmp = cs.getX() - restricteddx;
		cs.getPortrait()[0].attr('x', tmp - cs.getWidth()/2);
		cs.getPortrait()[1].attr('cx', tmp);
		cs.setX(tmp);
	});

	if (firstCritterSelectable.getPortrait()[0].attr('x') + 100 > 0) {
		rightArrow.show();
	} else if (lastCritterSelectable.getPortrait()[0].attr('x') < selectableAreaWidth) {
		leftArrow.show();
	} else {
		arrows.show();
	}

	selectKnob.attr('x', selectKnob.attr('x') + restricteddx * knobMultiplier);
	scrollKnob.attr('x', scrollKnob.attr('x') + restricteddx * knobMultiplier);
	titleScreen.attr('x', titleScreen.attr('x') + restricteddx * -titleScreenMultiplier);
});
});

var nameRect = critterSelectPaper.rect(365,210,90,36).attr('fill','yellow');
var nameText = critterSelectPaper.text(410,227,"Hawk").attr('font-size',20).attr('font-family',buttonFont);
var namePlaque = critterSelectPaper.set(nameRect,nameText);
var comingSoonRect = critterSelectPaper.rect(210,480,180,40).attr('fill','yellow');
var comingSoonText = critterSelectPaper.text(300,500,"Coming soon...").attr('font-size',20).attr('font-family',buttonFont);
var comingSoon = critterSelectPaper.set(comingSoonRect,comingSoonText);
namePlaque.hide();
comingSoon.hide();
var selectInitHappened = false;
function selectInit(str) {
	selectInitHappened = true;
	var critterStr = str.split('~')[0];
	var bigTokens = critterStr.split('|');
	for (var i = 1; i < bigTokens.length; i++) {
		var tokens = bigTokens[i].split(',');

		critterSelectables.forEach(function(cs){

			if (cs.getName() == tokens[0].toLowerCase()) {
				var hoverables = critterSelectPaper.set(cs.getPortrait(), cs.getMiniCritterSelectable().getSelectable());
				if (tokens[1] == "comingsoon") {
					hoverables.hover(
					function() {
						nameText.attr('text',cs.getName());
						nameText.node.setAttribute("class","donthighlight");
						namePlaque.show();
						comingSoon.show();
						comingSoonText.node.setAttribute("class","donthighlight");
						document.body.style.cursor = 'pointer';
						cs.getFullImage().show();
						critterSelectHelpText.hide();
						nameRect.attr({'x':nameText.getBBox().x - 10, 'y':nameText.getBBox().y -3, 'width':nameText.getBBox().width + 20, 'height':nameText.getBBox().height + 6});
					},
					function () {
						namePlaque.hide();
						comingSoon.hide();
						document.body.style.cursor = 'auto';
						cs.getFullImage().hide();
					});

				} else {
				cs.getAbilTypes()[0] = tokens[1];
				cs.getAbilInfo()[0] = tokens[2];
				cs.getAbilTypes()[1] = tokens[3];
				cs.getAbilInfo()[1] = tokens[4];
				cs.getAbilTypes()[2] = tokens[5];
				cs.getAbilInfo()[2] = tokens[6];
				cs.getAbilTypes()[3] = tokens[7];
				cs.getAbilInfo()[3] = tokens[8];
				cs.getAbilInfo()[4] = tokens[9];

				hoverables.hover(
				function() {
				nameText.attr('text',cs.getName());
				nameText.node.setAttribute("class","donthighlight");
				namePlaque.show();
				document.body.style.cursor = 'pointer';
					abilRects.forEach(function(a) {
						a.getRect().show();
					});
					cs.getFullImage().show();
					critterSelectHelpText.hide();
					setColorBasedOnType(abil1Rect, cs.getAbilTypes()[0]);
					abil1Rect.setInfo(cs.getAbilInfo()[0]);
					setColorBasedOnType(abil2Rect, cs.getAbilTypes()[1]);
					abil2Rect.setInfo(cs.getAbilInfo()[1]);
					setColorBasedOnType(abil3Rect, cs.getAbilTypes()[2]);
					abil3Rect.setInfo(cs.getAbilInfo()[2]);
					setColorBasedOnType(abil4Rect, cs.getAbilTypes()[3]);
					abil4Rect.setInfo(cs.getAbilInfo()[3]);
					setColorBasedOnType(bpRect, "passive");
					bpRect.setInfo(cs.getAbilInfo()[4]);
					nameRect.attr({'x':nameText.getBBox().x - 10, 'y':nameText.getBBox().y -3, 'width':nameText.getBBox().width + 20, 'height':nameText.getBBox().height + 6});
				},
				function () {
					namePlaque.hide();
					cs.getFullImage().hide();
					document.body.style.cursor = 'auto';
					abilRects.forEach(function(a) {
						a.getRect().hide();
						a.setInfo("");
					});
				});
				}
			}
		});
	}

	var itemStr = str.split('~')[1];
	var itemSplit = itemStr.split('|');


	for (var i = 0; i < itemSplit.length - 1; i++) {
		var infoSplit = itemSplit[i].split(',');
		itemSelectables.forEach(function(is){
			if (is.getName() == infoSplit[0]) {
				is.setInfo(infoSplit[1]);
				var itemHoverables = critterSelectPaper.set();
				itemHoverables.push(is.getPic(), is.getMiniItemSelectable().getSelectable());
				itemHoverables.hover(
				function(){
					is.getHoverImage().show();
					is.getHoverBox().show();
					is.getHoverBox().toFront();
					is.getHoverImage().toFront();
					critterSelectHelpText.hide();
					is.getItemRectBackground().show();
					document.body.style.cursor = 'pointer';
					itemRect.setInfo(is.getInfo());
					itemRect.getRect().show();
				},
				function(){
					document.body.style.cursor = 'auto';
					is.getItemRectBackground().hide();
					is.getHoverImage().hide();
					is.getHoverBox().hide();
					itemRect.setInfo("");
					itemRect.getRect().hide();
				});
			}
		});
	}
}

function setColorBasedOnType(infobox, type) {
	   if (type == "attack") {
         infobox.getRect().attr("fill", attackColor);
    } else if (type == "support") {
         infobox.getRect().attr("fill", supportColor);
    } else if (type == "block") {
         infobox.getRect().attr("fill", blockColor);
    } else if (type == "passive") {
   	 	infobox.getRect().attr("fill", moveColor);
    }
}

critterSelectables.forEach(function(fs) {
	if (!fs.isComingSoon()) {

	fs.getPortrait().click(function() {


	searchButton.disable();
	battleAiButton.disable();


		if (!fs.getSelected()) {
			if (crittersSelected.length < 4) {

				fs.setSelected(true);
				crittersSelected.push(fs.getName());
				selectedCritters.push(fs);
				var cx = fs.getPortrait()[1].attr('cx');
				var cy = fs.getPortrait()[1].attr('cy');
				var oldKnobX = selectKnob.attr('x');
				var oldKnobW = selectKnob.attr('width');
				var newKnobW = WIDTH / ((critterSelectables.length - crittersSelected.length) / (critterSelectables.length /2));
				var oldpct = oldKnobX / (WIDTH - oldKnobW);
				fs.setPct(oldpct);
				knobMultiplier = WIDTH / ((critterSelectables.length - crittersSelected.length) * 115);
				titleScreenMultiplier = 72/(WIDTH - newKnobW) * knobMultiplier;
				var newScrollKnobX = scrollKnob.attr('x') - (newKnobW - oldKnobW) * oldpct;
				var newSelectKnobX = selectKnob.attr('x') - (newKnobW - oldKnobW) * oldpct;
				scrollXOffset += 57 - 115*oldpct;
				scrollKnob.attr({'x': newScrollKnobX, 'width': newKnobW});
				var selectKnobBlock = critterSelectPaper.rect(0,selectKnob.attr('y'), WIDTH, selectKnob.attr('height')).attr({'stroke-opacity':0,'fill-opacity':0, 'fill':'yellow'});
				scrollKnob.hide();
				selectKnob.animate({'x': newSelectKnobX, 'width': newKnobW}, 100);
				fs.getPortrait()[0].animate({transform : 's0'},100);
				fs.getPortrait()[1].animate({transform : 's0'},100, function(){
					fs.getPortrait().hide();

				});
				fs.getMiniCritterSelectable().show();

				var onleft = true;
				for (var i = 0; i < critterSelectables.length; i++) {
					if (critterSelectables[i] == fs) {
						onleft = false;
					} else if (onleft) {
						critterSelectables[i].setX(critterSelectables[i].getX() + 115 * oldpct);
						critterSelectables[i].getPortrait()[0].animate({x:critterSelectables[i].getX() - critterSelectables[i].getWidth()/2}, 200, ">");
						critterSelectables[i].getPortrait()[1].animate({cx:critterSelectables[i].getX()}, 200, ">");
					} else {
						critterSelectables[i].setX(critterSelectables[i].getX() - 115 * (1 -oldpct));
						critterSelectables[i].getPortrait()[0].animate({x:critterSelectables[i].getX() - critterSelectables[i].getWidth()/2}, 200, ">");
						critterSelectables[i].getPortrait()[1].animate({cx:critterSelectables[i].getX()}, 200, ">", function(){selectKnobBlock.remove(); scrollKnob.show()});
					}
				}
			}
			if (crittersSelected.length == 4 && itemsSelected.length == 3 && !searching) {
				searchButton.enable();
				battleAiButton.enable();
			}



		} else {


			fs.setSelected(false);
			var index = crittersSelected.indexOf(fs.getName());
			if (searching){
				searchCancel();
			}
			searchButton.disable();
			battleAiButton.disable();
			selectedCritters.splice(index,1);
			crittersSelected.splice(index,1);
		}


	});
	}
});

function searchCancel() {
	ws.send("searchqueue,remove," + id);
	searching = false;
	searchButton.getText().attr('text', 'Vs. Player').attr("font-size", 18);
}

function CritterSelectable(imageName, pos, name, fullImage, backgroundImage) {
	this.x = selectableAreaWidth /2 - (totalCritters/2) *  115 + 115 *pos + 55;
	this.setX = function(x) {this.x = x};
	this.getX = function() {return this.x};
	this.y = 70;
	this.width = 100;
	this.getWidth = function() {return this.width};
	this.image = critterScrollPaper.image(imageName, this.x - 50 , this.y - 50, 100, 100);
	this.circle = critterScrollPaper.circle(this.x, this.y, 50).attr("fill", "#fff").attr('fill-opacity', '0').attr('stroke-width', '3').attr('stroke', '#704000');
	this.portrait = critterScrollPaper.set(this.image, this.circle);
	this.fullImage = fullImage;
	this.getFullImage = function() {return this.fullImage;};
	fullImage.hide();
	this.name = name;
	this.abilInfo = [];
	this.abilTypes = [];
	this.setAbilInfo = function(abilInfo) {this.abilInfo = abilInfo;};
	this.getAbilInfo = function() {return this.abilInfo;};
	this.setAbilTypes = function(abilTypes) {this.abilTypes = abilTypes;};
	this.getAbilTypes = function() {return this.abilTypes;};
	this.comingsoon = false;
	this.isComingSoon = function() {return this.comingsoon};
	this.setComingSoon = function(comingsoon) {this.comingsoon = comingsoon};
	this.abilRectFullBackground = backgroundImage;
	this.pct = 0.5;
	this.getPct = function(){return this.pct}
	this.setPct = function(pct){this.pct = pct}
	this.pos = pos;
	this.getPos = function() {return this.pos;};

	this.abilRectFullBackground.attr({'x': WIDTH/2 - 800 - HEIGHT/2, 'y': -1200, 'height': 1600 + HEIGHT, 'width': 1600 + HEIGHT}).hide();


	this.miniCritterSelectable;

	this.setMiniCritterSelectable = function(miniCritterSelectable) {
		this.miniCritterSelectable = miniCritterSelectable;
	}
	this.getMiniCritterSelectable = function() {
		return this.miniCritterSelectable;
	}

	this.selected = false;

	this.setSelected = function(selected) {this.selected = selected}
	this.getSelected = function () {return this.selected};
	this.getPortrait = function () {return this.portrait};
	this.getCircle = function(){return this.circle};
	this.getName = function(){return this.name};
}

function ItemSelectable(imageName, pos, name, hoverImage, hoverBox, pressedImage, backgroundImage) {

	this.x = WIDTH/2 + 10 - 110*3 + 110*pos;
	this.getX = function(){return this.x}
	this.setX = function(x){this.x = x}
	this.y = 570;
	this.image = critterSelectPaper.image(imageName, this.x, this.y, 100, 100);
//	var itemRectBackground = critterSelectPaper.image(imageName, WIDTH/2 - HEIGHT/2-150, -150,  975, 975);
	var itemRectBackground = backgroundImage.hide();
	itemRectBackground.attr({"clip-rect": itemRectX + " " + itemRectY + " " + itemRectWidth + " " + itemRectHeight, 'opacity': 0.4}).hide();
	this.getItemRectBackground = function() {return itemRectBackground};
	this.pressedImage = pressedImage;
	this.pressedImage.attr({x: this.x, y: this.y, width: 100, height: 100});
	this.pic = critterSelectPaper.set(this.image, this.pressedImage);
	this.name = name;
	this.getName = function() {return this.name};
	this.pressedImage.hide();
	hoverImage.hide();
	hoverBox.hide();
	this.getHoverImage = function() {return hoverImage};
	this.getHoverBox = function() {return hoverBox};
	this.getImage = function() {return this.image};
	this.getPressedImage = function() {return this.pressedImage};
	this.miniItemSelectable;
	this.getMiniItemSelectable = function() {return this.miniItemSelectable};
	this.setMiniItemSelectable = function(miniItemSelectable) {this.miniItemSelectable = miniItemSelectable};
	this.getPos = function(){return pos};
	this.info = "";
	this.setInfo = function(info) {this.info = info};
	this.getInfo = function() {return this.info};
	this.selected = false;
	this.setSelected = function(selected) {this.selected = selected};
	this.getSelected = function () {return this.selected};
	this.getPic = function () {return this.pic};
	this.getName = function(){return this.name};

}




//-----------------------------------------------------------------------------------------------------------------------------------------------------------



gridPaper.setViewBox(0,0,0,0,true);
var initialCalcQueue = true;

//gridPaper.setViewBox(0,0,1680,984,true);
//gridPaper.setSize(1680, 984)
//var background = gridPaper.rect(0,0,0,0);


var background = gridPaper.image("assets/backgrounds/background.png", 0,0,1080,675);
var backgroundBottom = gridPaper.image("assets/backgrounds/backgroundbottom.png", 0,HEIGHT/2 - 40,1080,675 - (HEIGHT/2 - 40));
var leftPortraitFrames = gridPaper.image("assets/misc/portraitframes.png", 0,0,366,152).hide();
var rightPortraitFrames = gridPaper.image("assets/misc/portraitframesright.png", WIDTH-366, 0, 366, 152).hide();
var iconY = 330;

var actionBarFrame = gridPaper.image("assets/actionbar/actionbarframe.png", 120, iconY - 12, WIDTH - 240, 90).hide();
var SIDE;
var sideCritters;

var calculating = false;
var positionHelpText = gridPaper.text(WIDTH/2, HEIGHT/2 + 20, "Drag and drop your Fighter Tokens to set their initial position").attr({"font-size": 20, "font-family": tipFont});
positionHelpText.node.setAttribute("class","donthighlight");
var abilityPaper;
var abilities = [];
var icons = [];
var locked = false;
var initialPhase = true;
var actionQueueStr;
var possibleMovesStr;
var abilitySelected = false;

var item1;
var item2;
var item3;
var items = [item1, item2, item3];
var otherItems = [];
var itemsChosen = [];
var itemBeingDragged = false;


//var infoBoxBackground = gridPaper.image("assets/gameinfobackground.png", 75, 465, (WIDTH/2 - 240) - 75, HEIGHT-480);

var logBackground = gridPaper.image("assets/backgrounds/chatbackground.png", WIDTH/2 + 260, 430, WIDTH/2 - 240 - 50, HEIGHT-445).toFront().hide();
hideLogButtonsAndText();
var log = document.getElementById('wrapper');
log.style.width = "225px";
log.style.height = HEIGHT - 450 + "px";
var leftPos = paperX + WIDTH/2 + 270;
var topPos = paperY + 435;
log.style.left = leftPos + "px";

log.style.top = topPos + "px";
// document.getElementsByName('message')[0].style.width = "225px";


var leftCritters = [];
var rightCritters = [];
var critters = [];

function startGame() {
	if (crittersSelected.length == 0) {
		if (id%2 == 0) {
			ws.send("select," + critterSelectables[1].getName() + "," + critterSelectables[4].getName() + "," + critterSelectables[2].getName() + "," + critterSelectables[7].getName() + "," + itemSelectables[0].getName() + "," + itemSelectables[1].getName() + "," + itemSelectables[2].getName() + "," + id);
		} else {
			ws.send("select," + critterSelectables[0].getName() + "," + critterSelectables[6].getName() + "," + critterSelectables[3].getName() + "," + critterSelectables[5].getName() + "," + itemSelectables[3].getName() + "," + itemSelectables[4].getName() + "," + itemSelectables[5].getName() + "," + id);
		}

	}
	else {
		ws.send("select," + crittersSelected[0] + "," + crittersSelected[1] + "," + crittersSelected[2] + "," + crittersSelected[3] + "," + itemsSelected[0] + "," + itemsSelected[1] + "," + itemsSelected[2] + "," + id);
	}
	searching = false;
	gridPaper.setViewBox(0,0,WIDTH,HEIGHT,true);
	itemsChosen = itemsSelected;
	document.getElementById('wrapper').style.visibility="visible";
//	document.getElementById('play').parentNode.removeChild(document.getElementById('play'));
	critterSelectPaper.remove();
	critterScrollPaper.remove();

}

// var rightBar = gridPaper.rect(WIDTH/2-100, 7, 200, 20).attr('fill','blue');
// var leftBar = gridPaper.rect(WIDTH/2-100, 7, 100, 20).attr('fill','orange');
var moraleCircles = gridPaper.set();
for (var i = 0; i < 10; i++) {
	var circle = gridPaper.circle(WIDTH/2-100 + i * 22, 9, 8);
	if (i < 5) {
		circle.attr('fill', '#FFD997');
	} else {
		circle.attr('fill', '#98BEFF');
	}

	moraleCircles.push(circle);

}
moraleCircles.hover(function () {
	infoBox.setInfo("Morale determines the likelihood of \ngoing first when two actions are \nscheduled to execute at the same \ntime. Unblocked attacks raise morale.");
})
moraleCircles.hide();


function moraleBar(str) {
	var tokens = str.split(",");
	if (tokens[2] == "left") {
		for (var i = 0; i < 10; i++) {
			if (i < tokens[1]) {
				moraleCircles[i].attr('fill', '#FFD997');
			} else {
				moraleCircles[i].attr('fill', '#98BEFF');
			}
		}
	} else {
		for (var i = 9; i >= 0; i--) {
			if (i > 9 - tokens[1]) {
				moraleCircles[i].attr('fill', '#98BEFF');
			} else {
				moraleCircles[i].attr('fill', '#FFD997');
			}
		}
	}
}



function actionOptions(str) {
	var tokens = str.split(",");
	var toShowOptions = [];
	for (var i = 3; i < tokens.length; i = i + 2) {
		toShowOptions.push(identifyCritter(tokens[i], tokens[i+1]));
	}
	if (tokens[1] == "remove") {
		showPossibleMoves(possibleMovesStr, false);
		abilitySelected = false;
		abilities.forEach(function(a){
			if (tokens[2] == a.getName()) {
				a.setClicked(false);
				a.getRect().attr("fill-opacity", 0);
				a.getRect().attr("fill", "white");
    		}
		});
		critters.forEach(function(f) {
			f.hideAttackOption();
			f.hideSupportOption();
			f.hideBlockOption();
			f.getClickBox().unhover();
			f.getClickBox().hover(
				function() {
					document.body.style.cursor = 'pointer';
					f.updateInfoBox();
					infoBox.show();
				},
				function () {
					document.body.style.cursor = 'auto';
					infoBox.hide(false);
				}
			);
			f.getPortraitClickBox().unhover();
			f.getPortraitClickBox().hover(
				function() {
					document.body.style.cursor = 'pointer';
					f.updateInfoBox();
					infoBox.show();
				},
				function () {
					document.body.style.cursor = 'auto';
					infoBox.hide(false);
				}
		);
		});
	} else {
		showPossibleMoves(",no moves", false);
		abilitySelected = true;
		abilities.forEach(function(a){
			if (a.getName() == tokens[2]) {
				a.setClicked(true);
    	  		a.getRect().attr("fill", "#999999");
				a.getRect().attr("fill-opacity", 0.5);
			}
		 });
		toShowOptions.forEach(function(f){
		var st = gridPaper.set();

		st.push(f.getClickBox());

		st.push(f.getPortraitClickBox());

		st.forEach(function (a) {

		a.hover(

			function() {
				if (tokens[1] == "attack") {
					f.showAttackOption();
				} else if (tokens[1] == "support") {
					f.showSupportOption();
				} else if (tokens[1] == "block") {
					f.showBlockOption();
				}
			},
			function () {
				if (tokens[1] == "attack") {
					f.hideAttackOption();
				} else if (tokens[1] == "support") {
					f.hideSupportOption();
				} else if (tokens[1] == "block") {
					f.hideBlockOption();
				}
			}
		);
	});
	});
	}
}

function updateChatLog(str) {

	var tokens = str.split(",");
	var str = tokens[1] + ": " + tokens[2];
	var p = document.createElement("p");
	p.innerHTML = str;
	p.style.fontFamily = logTextFont;
	p.style.fontSize = 10;
	textArea = document.getElementById("chattextarea");
	textArea.appendChild(p);
	textArea.scrollTop = textArea.scrollHeight;

}
function updateActionLog(str) {
	var tokens = str.split(",");
	var str = tokens[1];
	var p = document.createElement("p");
	p.innerHTML = str;
	p.style.fontFamily = logTextFont;
	p.style.fontSize = 10;
	textArea = document.getElementById("actiontextarea");
	textArea.appendChild(p);
	textArea.scrollTop = textArea.scrollHeight;
}

function chatLogButton() {
	document.getElementById("chatlog").disabled=true;
	document.getElementById("actionlog").disabled=false;
	document.getElementById("actiontextarea").style.visibility="hidden";
	document.getElementById("chattextarea").style.visibility="visible";

}
function actionLogButton() {
	document.getElementById("chatlog").disabled=false;
	document.getElementById("actionlog").disabled=true;
	document.getElementById("chattextarea").style.visibility="hidden";
	document.getElementById("actiontextarea").style.visibility="visible";

}
function hideLogButtonsAndText() {
	document.getElementById("actionlog").style.visibility="hidden";
	document.getElementById("chatlog").style.visibility="hidden";
	document.getElementById("chattextarea").style.visibility="hidden";
	document.getElementById("actiontextarea").style.visibility="hidden";
	document.getElementById("usermsg").style.visibility="hidden";
}
function showLogButtonsAndText() {
	document.getElementById("actionlog").style.visibility="visible";
	document.getElementById("chatlog").style.visibility="visible";
	if (document.getElementById("chatlog").disabled) {
		document.getElementById("chattextarea").style.visibility="visible";
	} else {
		document.getElementById("actiontextarea").style.visibility="visible";
	}
	document.getElementById("usermsg").style.visibility="visible";

}

function reportBug(name, message) {
	if (name != "" && message != "") {
		ws.send("bugreport," + name + "," + message);
		alert("An email was sent to Gabe, Thanks!");
 		document.getElementsByName('bugreport')[0].reset();
	}
}

function test(str) {

 document.getElementsByName('message')[0].reset();
 ws.send("chat," + str + ",chat,chat,chat" + "," + id);

 return false;
}

// var layoutrect = gridPaper.rect(75, 465, WIDTH - 150, HEIGHT - 480).attr("fill", "purple").attr("fill-opacity", "0.1");
var infoBoxBackground = gridPaper.image("assets/backgrounds/gameinfobackground.png", 30, 430, (WIDTH/2 - 240) - 50, HEIGHT-445).hide();
var infoBox = new InfoBox(30, 430, (WIDTH/2 - 240) - 50, HEIGHT-445, gridPaper, "Game Info", 30,13.5);
infoBox.hide(true);
infoBox.getRect().remove();




function InfoBox(x, y, width, height, paper, title, distanceFromTop, fontSize) {
	this.x = x;
	this.width = width;
	this.y = y;
	this.height = height;
	this.fontSize = fontSize;
	this.rect = paper.rect(this.x,this.y,this.width,this.height).attr("fill", "black").attr("fill-opacity", "0.1");
	this.getRect = function() {return this.rect;}
	this.getY = function() {return this.y;}
	this.info = paper.text(this.x + 5, this.y + 30, "").
		attr({"text-anchor":"start", "font-family": infoBoxTextFont});
	this.setColor = function(color) {this.rect.attr("fill",color)};
	this.setOpacity = function(opacity){this.rect.attr("fill-opacity", opacity);}
	this.setInfo = function(str) {this.info.remove();this.info = paper.text(this.x + 5, this.y + distanceFromTop, str).attr("text-anchor","start").attr("font-size", this.fontSize).attr("font-family", infoBoxTextFont); alignTop(this.info); this.info.node.setAttribute("class","donthighlight");}
	this.hide = function(titleToo) {this.rect.hide(); this.info.hide(); if (titleToo) {this.title.hide();}}
	this.show = function() {this.rect.show(); this.title.show(); this.info.show();}
	this.title = paper.text(this.x + this.width/2, this.y + 10, title)
	.attr({"font-size": 16, "font-family": infoBoxTitleFont});
	this.title.node.setAttribute("class","donthighlight");
	this.info.node.setAttribute("class","donthighlight");
	this.setFontSize = function(fs) {this.info.attr('font-size', fs);
									this.fontSize = fs;}
	this.move = function(y) {
		this.y = y;
		this.height = this.rect.attr('height');
		this.title.attr('y', this.y + 10);
		this.info.attr('y', this.y + 30);
		alignTop(this.info);
	};
}



function revive(str) {
	var tokens = str.split(',');
	var critter = identifyCritter(tokens[1], tokens[3]);

	move("a," + tokens[1] + ',' + tokens[3] + ',' + tokens[2] + ',' + 300);

    setTimeout(function() {
		critter.getImage().show();
		critter.getAltImage().show();
		critter.getClickBox().show();
		critter.getScreen().hide();
	},250);

}

function death(str) {
	var tokens = str.split(',');
	var critter = identifyCritter(tokens[1], tokens[2]);
	critter.getImages().hide();
	critter.getImage().hide();
	critter.getClickBox().hide();
	critter.getScreen().show();
	changeBars("energy," + tokens[1] + "," + tokens[2] + "," + 0);
	changeBars("health," + tokens[1] + "," + tokens[2] + "," + 0);
	var origLength = critter.getEffecttts().length;
	for (var i = 0; i < origLength; i++) {
		createEffectTT("effecttt,remove," + tokens[1] + "," + tokens[2] + "," + 0);
	}
}

function initialPhaseEnd(str) {
	positionHelpText.hide();
    		readyButton.enable();
    		readyButton.getRect()[1].attr('text','Fight!');
	leftPortraitFrames.show();
	rightPortraitFrames.show();
	var tokens = str.split(',');
	initialPhase = false;

	var f1 = identifyCritter(tokens[1], tokens[2]);
	var s1 = identifySpot(tokens[3]);
	var f2 = identifyCritter(tokens[4], tokens[5]);
	var s2 = identifySpot(tokens[6]);
	var f3 = identifyCritter(tokens[7], tokens[8]);
	var s3 = identifySpot(tokens[9]);
	var f4 = identifyCritter(tokens[10], tokens[11]);
	var s4 = identifySpot(tokens[12]);
	var f5 = identifyCritter(tokens[13], tokens[14]);
	var s5 = identifySpot(tokens[15]);
	var f6 = identifyCritter(tokens[16], tokens[17]);
	var s6 = identifySpot(tokens[18]);
	var f7 = identifyCritter(tokens[19], tokens[20]);
	var s7 = identifySpot(tokens[21]);
	var f8 = identifyCritter(tokens[22], tokens[23]);
	var s8 = identifySpot(tokens[24]);
	var ftrs = [f1,f2,f3,f4,f5,f6,f7,f8];
	var spts = [s1,s2,s3,s4,s5,s6,s7,s8];
	spots.forEach(function(s) {
		s.getPolygon().attr("stroke-width", 0);
	});
	for(var i = 0; i < 8; i++) {
		ftrs[i].setX(spts[i].getCritterX());
		ftrs[i].setY(spts[i].getCritterY());
		if (ftrs[i].getY() == midCritterY) {
			ftrs[i].getImage().animate({transform: 's0.95'});
			ftrs[i].getClickBox().attr({x: ftrs[i].getX() + 20, y: ftrs[i].getY() + midyBoost});
			ftrs[i].getImages().forEach(function(fi){
				fi.animate({transform: 's0.95'});
			});
			ftrs[i].getClickBox().animate({transform: 's0.95'});

    	} else if (ftrs[i].getY() == benchCritterY) {
    		ftrs[i].getImage().animate({transform: 's0.97'});
    		ftrs[i].getClickBox().attr({x: ftrs[i].getX() + 20, y: ftrs[i].getY() + benchyBoost});
			ftrs[i].getImages().forEach(function(fi){
				fi.animate({transform: 's0.97'});
			});
			ftrs[i].getClickBox().animate({transform: 's0.97'});
    	} else if (ftrs[i].getY() == topCritterY) {
			ftrs[i].getImage().animate({transform: 's0.90'});
			ftrs[i].getClickBox().attr({x: ftrs[i].getX() + 20, y: ftrs[i].getY() + topyBoost});
			ftrs[i].getImages().forEach(function(fi){
				fi.animate({transform: 's0.90'});
			});
			ftrs[i].getClickBox().animate({transform: 's0.90'});
    	} else if (ftrs[i].getY() == botCritterY) {
    		ftrs[i].getClickBox().attr({x: ftrs[i].getX() + 20, y: ftrs[i].getY() + botyBoost});

    	}

		ftrs[i].getToken().getPortrait().hide();

    	if (ftrs[i].getY() == leftTopBackSpot.getCritterY()) {
    		ftrs[i].getImage().insertBefore(topRow);
    		ftrs[i].getImages().insertBefore(topRow);
		}  else if (ftrs[i].getY() == leftMiddleBackSpot.getCritterY()) {
    		ftrs[i].getImage().insertBefore(middleRow);
    		ftrs[i].getImages().insertBefore(middleRow);
    	} else if (ftrs[i].getY() == benchCritterY) {
    		ftrs[i].getImage().insertBefore(benchRow);
    		ftrs[i].getImages().insertBefore(benchRow);
    	} else {
    		ftrs[i].getImage().insertBefore(botRow);
    		ftrs[i].getImages().insertBefore(botRow);
    	}
    	ftrs[i].getPortraitClickBox().show();
    	ftrs[i].getClickBox().show();
		ftrs[i].getImages().attr('x', spts[i].getCritterX()).attr('y', spts[i].getCritterY());
		ftrs[i].getImage().attr('x', spts[i].getCritterX()).attr('y', spts[i].getCritterY()).show();
		ftrs[i].getAltImage().show();
		ftrs[i].getPortrait().show();
		moraleCircles.show();

	}
	ftrs.forEach(function (f) {
		if (f.getY() == topCritterY) {
			f.getClickBox().toFront();
		}
	});
	ftrs.forEach(function (f) {
		if (f.getY() == leftMiddleFrontSpot.getCritterY()) {
			f.getClickBox().toFront();
		}
	});
	ftrs.forEach(function (f) {
		if (f.getY() == leftBottomFrontSpot.getCritterY()) {
			f.getClickBox().toFront();
		}
	});
	actionBarFrame.show();
	logBackground.show();
	infoBoxBackground.show();
	showLogButtonsAndText();
	infoBox.show();
	locked = false;
}

function showGrid(str) {
	alert(str);
	var sideSpots;
	var tokens = str.split(",");
	SIDE = tokens[1];
    	if (tokens[1] == "left") {
    		leftSpots.forEach(function(s) {
    			s.getPolygon().attr("stroke-width", "1");
    		});
    		sideCritters = leftCritters;
    		sideSpots = leftSpots;
    	} else {
    	    rightSpots.forEach(function(s) {
    			s.getPolygon().attr("stroke-width", "1");
    		});
    		sideCritters = rightCritters;
    		sideSpots = rightSpots;
    	}
    	sideCritters[3].getToken().getPortrait().toFront();
    	if (SIDE == "left") {
			sideCritters[3].getToken().getPortrait()[0].attr({x : leftBench.getCritterX() + 70, y : benchCritterY + 130});
			sideCritters[3].getToken().getPortrait()[1].attr({cx : leftBench.getCritterX() + 95, cy : benchCritterY + 155});
			sideCritters[3].getToken().setSpot(leftBench);
			leftBench.setPositionedAt(true);
		} else {
			sideCritters[3].getToken().getPortrait()[0].attr({x : rightBench.getCritterX() + 70, y : benchCritterY + 130});
			sideCritters[3].getToken().getPortrait()[1].attr({cx : rightBench.getCritterX() + 95, cy : benchCritterY + 155});
			sideCritters[3].getToken().setSpot(rightBench);
			rightBench.setPositionedAt(true);
		}

    	sideCritters.forEach(function(sf) {
			var lx = 0;
			var ly = 0;
    		sf.getToken().getPortrait().drag(
    		function(dx, dy) {
    			if (!locked) {
    				sf.getToken().getPortrait().transform('t' + dx + ',' + dy);
    				lx = dx;
    				ly = dy;
    			}
    		},
			function() {


			},
			function (dx, dy) {
			if (!locked) {
				var token = sf.getToken();
				token.getPortrait().transform('t' + -dx + ',' + -dy);
				var isCritterAtBench = true;
				var spotFound = false;
				for (var i = 0; i < sideSpots.length; i++) {
					if (sideSpots[i].getPolygon().isPointInside(token.getPortrait()[1].attr('cx') + lx, token.getPortrait()[1].attr('cy') + ly) && !sideSpots[i].getPositionedAt() && !spotFound) {
						spotFound = true;
						token.getPortrait().toFront();
						if (token.getSpot() != null) {
							token.getSpot().setPositionedAt(false);
						}
						token.setSpot(sideSpots[i]);
						sideSpots[i].setPositionedAt(true);
						if (SIDE == "left") {
							token.getPortrait()[0].attr({x : sideSpots[i].getCritterX() + (critterSize - 140), y : sideSpots[i].getCritterY() + (critterSize - 40)});
							token.getPortrait()[1].attr({cx : sideSpots[i].getCritterX() + (critterSize - 115), cy : sideSpots[i].getCritterY() + (critterSize - 15)});
						} else {
							token.getPortrait()[0].attr({x : sideSpots[i].getCritterX() + (critterSize - 100), y : sideSpots[i].getCritterY() + (critterSize - 40)});
							token.getPortrait()[1].attr({cx : sideSpots[i].getCritterX() + (critterSize - 75), cy : sideSpots[i].getCritterY() + (critterSize - 15)});
						}
					}
					if (sideSpots[i].getName().includes('Bench')) {
						if (sideSpots[i].getPositionedAt()){
							sideSpots[i].getPolygon().attr({"fill-opacity": 0});
							isCritterAtBench = true;
						} else {
							sideSpots[i].getPolygon().attr({"fill": "red", "fill-opacity": 0.3});
							isCritterAtBench = false;
						}
					}
				}
				var allPlaced = true;
				sideCritters.forEach(function(sc) {
	 				if (sc.getToken().getSpot() == null) {
	 					allPlaced = false;
	 				}
	 			});
	 			if (allPlaced && isCritterAtBench &&  !locked) {
	 				readyButton.enable();
	 			} else if (!locked) {
	 				readyButton.disable();
	 			}
			}
			}
		);
    	});

}

// var linerinos = gridPaper.set();
// for (var i = 0; i < WIDTH; i += 10) {
//	var l = gridPaper.path('M' + i + ',' + 0 + ' L ' + i + ',' + HEIGHT).attr("stroke-width", 1).attr("stroke-opacity", 0.3);
//	linerinos.push(l);
//}
//for (var i = 0; i < HEIGHT; i += 10) {
//	var l = gridPaper.path('M' + 0 + ',' + i + ' L ' + WIDTH + ',' + i).attr("stroke-width", 1).attr("stroke-opacity", 0.3);
//	linerinos.push(l);
//}


function critterSelect(str){
	var newCritter;
	var bigTokens = str.split("|");

    for (var i = 1; i < bigTokens.length -1; i++) {
        var tokens = bigTokens[i].split(",");
        newCritter = new Critter(tokens[0], tokens[1], tokens[2], tokens[3], i - 1, tokens[4], tokens[5]);
        newCritter.getClickBox().toFront();
	 leftPortraitFrames.toFront();
	 rightPortraitFrames.toFront();
   		critters.push(newCritter);
   		if (tokens[3] == "right") {
   			rightCritters.push(newCritter);
   		} else {
   			leftCritters.push(newCritter);
   		}
    }
    leftCritters.forEach(function(lf) {
    	lf.getPortraitClickBox().toFront();
    	lf.getClickBox().click(function() {
            if (!calculating) {
            	ws.send("click,critter," + lf.getName() + ",left" + "," + id);
        	}
        })
    });
    rightCritters.forEach(function(rf) {
    	rf.getPortraitClickBox().toFront();
    	rf.getClickBox().click(function() {
            if (!calculating) {
            	ws.send("click,critter," + rf.getName() + ",right" + "," + id);
        	}
        })
    });
}

function transition() {
    logBackground.attr({"clip-rect": (logBackground.attr('x') + " " + (logBackground.attr('y') + 110) + " " + logBackground.attr('width') + " " + (logBackground.attr('height') - 110))});
    infoBoxBackground.attr({"clip-rect": (infoBoxBackground.attr('x') + " " + (infoBoxBackground.attr('y') + 110) + " " + infoBoxBackground.attr('width') + " " + (infoBoxBackground.attr('height') - 110))});
    readyButton.hide();
    actionBarFrame.hide();
    critters.forEach(function(f) {
        	f.unindicateAttack();
        	f.unindicateSupport();
        	f.unindicateBlock();
   	});
   	infoBox.setFontSize(8.5);
   	log.style.height = HEIGHT - 590 + "px";
	infoBox.move(infoBox.getY() + 110);
	log.style.top = paperY + infoBox.getY() + "px";
    items.forEach(function(item) {
    	if(item.getPic() != null && !item.isQueued()) {
        		item.getPic().hide();
        }
    });
    opponentPaper.hide();
	myPaper.hide();
	displayAbilities("remove");
	upperRect.attr('y', actionBarFrame.attr('y')).show();
	lowerRect.attr('y', actionBarFrame.attr('y')).show();
	lowerRect.animate({y: lowerFrameY},750);
}

function calculateTurn(str) {

	var tokens = str.split(",");
    if (tokens[1] == "begin") {
		timeBar();
		var i = 0;
		spots.forEach(function(s) {
			s.unindicateMove();
			s.unindicateAttack();
			s.unindicateSupport();
		});
        items.forEach(function(item) {

        	if (item.isQueued()) {
        		item.getMiniPic().show();
        		var lineX = item.getLine().getPointAtLength().x;
        		item.getLine().attr('y', '480');
        		item.getPic().attr('y', '444');
       			item.getLine().toFront();
    		}
    		i++;
     	});
        calculating = true;

    } else {

    		actionBarFrame.show();
        	while(otherItems.length	> 0) {
        		otherItem = otherItems.pop();
        		otherItem.getPic().remove();
        		otherItem.getLine().remove();
        		otherItem.getMiniPic().remove();
        	}
       		critters.forEach(function(f) {

            	for (var i = 0; i < f.getEffecttts().length; i++) {
        			f.getEffecttts()[i].setDuration(f.getEffecttts()[i].getDuration() -1);
        		}
        	});


        	log.style.height = HEIGHT - 450 + "px";
        	logBackground.attr({"clip-rect": (logBackground.attr('x') + " " + logBackground.attr('y') + " " + logBackground.attr('width') + " " + logBackground.attr('height'))});
    		infoBoxBackground.attr({"clip-rect": (infoBoxBackground.attr('x') + " " + infoBoxBackground.attr('y') + " " + infoBoxBackground.attr('width') + " " + infoBoxBackground.attr('height'))});
        	infoBox.move(infoBox.getY() - 110);
        	infoBox.setFontSize(13.5);
        	log.style.top = topPos + "px";

        	document.getElementById("usermsg").style.visibility="visible";

        	for (var i = 0; i < items.length; i++) {
        		if (items[i].isQueued()) {
        			items.splice(i,1);
        		}
        	}
        	for (var i = 0; i < itemsChosen.length; i++) {
        		if (itemsChosen[i].isQueued()) {
        			itemsChosen.splice(i,1);
        		}
        	}
    items.forEach(function(item) {
    	if(item.getPic() != null && !item.isQueued()) {
        		item.getPic().show();
        }
    });
        	readyButton.enable();
    		readyButton.getRect()[1].attr('text','Fight!');
    		readyButton.show();
        	calculating = false;
        }

}


function otherItemDisplay(str) {

	var tokens = str.split(",");
	var otherItem = new Item(tokens[1], tokens[2], tokens[3], tokens[4]);
	otherItem.getPic().hide();
	var otherItemMiniPic = otherItem.getMiniPic();
	otherItemMiniPic.show();
	var newX = tokens[5] * 0.1*(WIDTH-260) + 130;

	var newNewX = newX;
	otherItems.forEach(function(it) {
		if (it.getPic()[0].attr('x') == newX && it != otherItem) {
			otherItems.forEach(function(ite) {
				if (ite.getPic()[0].attr('x') == newX + 30 && ite != otherItem) {
					newNewX += 30;
				}
			});
		newNewX += 30;
		}
	});

	otherItemMiniPic.attr('x', newNewX).attr('y', otherItemY);
	otherItem.getPic().forEach(function(n){
		n.attr('x', newNewX).attr('y', otherItemY);
	});

	otherItemMiniPic.hover(
		function() {
			document.body.style.cursor = 'pointer';
			infoBox.setInfo(otherItem.getDescription());
		},
		function () {
			document.body.style.cursor = 'auto';
			infoBox.setInfo("");
		}
	);
	line = gridPaper.path("M" + (newX) + " " + (iconY + 100) + " L " + (newX)  + " " + (otherItemY + 25)).attr('stroke', 'white').attr('stroke-width', '1');
	otherItem.setLine(line);
	otherItems.push(otherItem);

}

function initItems(str) {

	var bigTokens = str.split("|");
	if (!initialPhase) {
		var newItem;
	    for (var i = 1; i < bigTokens.length -1; i++) {
	    	var tokens = bigTokens[i].split(",");
	    	newItem = new Item(tokens[0], tokens[1], tokens[2], tokens[3]);
        	items.push(newItem);
        	itemsChosen.push(newItem);
   		}
   		newItem.getPic().hide();
   		    	newItem.getPic().click(function() {
    		if (!locked) {
    			if (itemsChosen.indexOf(newItem) != -1) {
    				itemsChosen[itemsChosen.indexOf(newItem)] = null;
    				newItem.getPic().attr({y : newItem.getY(), x: newItem.getX()});
    			}
    		}
    	});
    	newItem.getMiniPic().itemDraggable(newItem.getImage().attr('x'), newItem.getImage().attr('y'), newItem.getImage(), newItem);
		newItem.getPic().itemDraggable(newItem.getImage().attr('x'), newItem.getImage().attr('y'), newItem.getImage(), newItem);
		newItem.getPic().hover(
			function() {
				document.body.style.cursor = 'pointer';
				infoBox.setInfo(newItem.getDescription());
			},
			function () {
			document.body.style.cursor = 'auto';
			infoBox.setInfo("");
			}
		);

		newItem.getMiniPic().hover(
			function() {
				document.body.style.cursor = 'pointer';
				infoBox.setInfo(newItem.getDescription());
			},
			function () {
			document.body.style.cursor = 'auto';
			infoBox.setInfo("");
			}
		);
	} else {
    for (var i = 1; i < bigTokens.length -1; i++) {
        var tokens = bigTokens[i].split(",");
        items[i -1] = new Item(tokens[0], tokens[1], tokens[2], tokens[3]);
    }

    itemsChosen = items;
    items.forEach(function(item) {
    	item.getPic().click(function() {
    		if (!locked) {
    			if (itemsChosen.indexOf(item) != -1) {
    				itemsChosen[itemsChosen.indexOf(item)] = null;
    				item.getPic().attr({y : item.getY(), x: item.getX()});
    			}
    		}
    	});
    	item.getMiniPic().itemDraggable(item.getImage().attr('x'), item.getImage().attr('y'), item.getImage(), item);
		item.getPic().itemDraggable(item.getImage().attr('x'), item.getImage().attr('y'), item.getImage(), item);
		item.getPic().hover(
			function() {
				document.body.style.cursor = 'pointer';
				infoBox.setInfo(item.getDescription());
			},
			function () {
			document.body.style.cursor = 'auto';
			infoBox.setInfo("");
			}
		);

		item.getMiniPic().hover(
			function() {
				document.body.style.cursor = 'pointer';
				infoBox.setInfo(item.getDescription());
			},
			function () {
			document.body.style.cursor = 'auto';
			infoBox.setInfo("");
			}
		);
	});
	}


}

function Effect(x, y, imgName, source, duration, effect) {
	this.img = gridPaper.image("assets/EffectSigns/effect" + imgName, x, y - 19, 12, 12);
	this.img.hover(
		function() {
			document.body.style.cursor = 'pointer';
		},
		function () {
			document.body.style.cursor = 'auto';
		}
	);
	this.getImg = function(){return this.img;}
	this.source = source;
	this.duration = duration;
	this.effect = effect;
	this.description = imgName.substring(0, imgName.length - 4) + "\neffect: " + this.effect + "\nsource: " + this.source + "\nduration: " + this.duration;
	this.getDescription = function() {return this.description};
	this.getDuration = function() {return this.duration;}
	this.setDuration = function(duration) {this.duration = duration;
											this.description = imgName.substring(0, imgName.length - 4) + "\neffect: " + this.effect + "\nsource: " + this.source + "\nduration: " + this.duration;
											}
}

function createEffectTT(str) {
	var bigTokens = str.split("|");
    for (var i = 0; i < bigTokens.length; i++) {
        var tokens = bigTokens[i].split(",");
	if (tokens[1] == "create") {

	var critter = identifyCritter(tokens[4], tokens[5]);
	var portraitX = critter.getPortraitX();
	var portraitY = critter.getPortraitY();
	var x;
	if (tokens[5] == "right") {
		x = portraitX - 54 - 12 * tokens[8];
	} else if (tokens[5] == "left") {
		x = portraitX + 42 + 12 * tokens[8];
	}

	var newEffect = new Effect(x, portraitY - 5, tokens[2], tokens[3], tokens[6], tokens[7]);
	critter.getEffecttts().push(newEffect);

	newEffect.getImg().hover(
	function() {
		document.body.style.cursor = 'pointer';
		infoBox.setInfo(newEffect.getDescription());
	},
	function () {
		infoBox.setInfo("");
		document.body.style.cursor = 'auto';
	}
	);
	} else if (tokens[1] == "remove") {
		var critter = identifyCritter(tokens[2],tokens[3]);

		var effectttArrToRemove = critter.getEffecttts().splice(tokens[4],1);

		var effectttToRemove = effectttArrToRemove[0];


		for (var h = 0; h < 5; h++) {
			effectttToRemove.getImg().remove();
		}

		if (critter.getEffecttts()[i].getImg().attr('x') < 300) {
			for (var i = tokens[4]; i < critter.getEffecttts().length; i++) {
				var toShift = critter.getEffecttts()[i].getImg();
					toShift.attr('x', toShift.attr('x') - 15);

			}
		} else {
			for (var i = tokens[4]; i < critter.getEffecttts().length; i++) {
				var toShift = critter.getEffecttts()[i].getImg();
					toShift.attr('x', toShift.attr('x') + 15);

			}

		}
	}
	}

}

function triggerActionAnimation(type, targetType, target, side) {
	console.log(type + ', ' + targetType + ', ' + target  + ', ' + side );
	var ANIMATION_SPEED_MS = 120;


	var FRAME_WIDTH = 200;

	var FRAME_HEIGHT = 200;
	var TOTAL_FRAMES = 4;
	// Run 12.5 frames per second
	// 1. Create the image element on the paper. This is its permanent canvas location.
	var spritesheetFileName = "ATK_SPRITESHEET.png";
	if (type == "support") {
		spritesheetFileName = "SPRT_SPRITESHEET.png";
		TOTAL_FRAMES = 5;
	} else if (type == "block") {
		spritesheetFileName = "DEF_SPRITESHEET.png";
		TOTAL_FRAMES = 5;
	} else if (type == "move") {
		console.log("move exit animate ")
		return;
	}

	var DISPLAY_X;
	var DISPLAY_Y;
	console.log(targetType);
	if (targetType == "spot") {
	//	alert(target);
		DISPLAY_X = target.getCritterX();
		DISPLAY_Y = target.getCritterY();
	} else {
		DISPLAY_X = target.getImage().attr('x');
		DISPLAY_Y = target.getImage().attr('y');
	}

	var flashImage = gridPaper.image(
		"assets/" + spritesheetFileName,
	//	critter.getImage().attr('x'), // <--- Initial canvas X position
	0,
	0, // <--- Initial canvas Y position
		FRAME_WIDTH * TOTAL_FRAMES,
		FRAME_HEIGHT
	).attr({
		opacity: 0
	});

    var currentFrame = 0;

    // Set the image to its fixed starting position (defined in the setup) and make it visible
    flashImage.attr({
        opacity: 1,
        // We set the initial clip rect here. It's relative to the image's (100, 100) position.
        // So, the top-left corner of the clip box is at (0, 0) relative to the image element.
		x: DISPLAY_X,
		y: DISPLAY_Y,
        "clip-rect": DISPLAY_X + ", " + DISPLAY_Y + ", " + FRAME_WIDTH + ", " + FRAME_HEIGHT
    });
		if (side == "right") {
			var bbox = flashImage.getBBox();
			var centerX = DISPLAY_X + FRAME_WIDTH / 2;
			var centerY = DISPLAY_Y + FRAME_HEIGHT / 2;
		//	alert(centerX + ', ' + centerY);
			flashImage.transform("S-1,1," + centerX + "," + centerY);
		}
    var animationInterval = setInterval(function() {


        // if (currentFrame > TOTAL_FRAMES) {
        //     clearInterval(animationInterval);
        //     flashImage.animate({ opacity: 0 }, 100, function() {
        //         this.hide();
        //     });
        //     return;
        // }

        // --- THE CORRECTED CLIP CALCULATION ---

        // CLIP_OFFSET_X is the horizontal distance from the left edge of the sprite sheet
        // to the start of the current frame.
        var CLIP_OFFSET_X = currentFrame * FRAME_WIDTH;
		var newImageX = DISPLAY_X - CLIP_OFFSET_X;
        // The clip-rect uses these element-relative coordinates
        flashImage.attr({
            // clip-rect format: "clip-x, clip-y, clip-width, clip-height"
  //          "clip-rect": CLIP_OFFSET_X + ", 0, " + FRAME_WIDTH + ", " + FRAME_HEIGHT,
		 	x: newImageX
        });
		if (side == "right") {
			var bbox = flashImage.getBBox();
			var centerX = DISPLAY_X + FRAME_WIDTH / 2;
			var centerY = DISPLAY_Y + FRAME_HEIGHT / 2;
		//	alert(centerX + ', ' + centerY);
			flashImage.transform("S-1,1," + centerX + "," + centerY);
		}
	//	flashImage.transform('t' + (-CLIP_OFFSET_X) - ',' + 0);

        currentFrame++;
    }, ANIMATION_SPEED_MS);
}

function triggerSupportAnimation(critter, side) {
	var ANIMATION_SPEED_MS = 90;
	var DISPLAY_X = critter.getImage().attr('x');
	var DISPLAY_Y = critter.getImage().attr('y');

	var FRAME_WIDTH = 200;

	var FRAME_HEIGHT = 200;
	var TOTAL_FRAMES = 5;
	// Run 12.5 frames per second
	// 1. Create the image element on the paper. This is its permanent canvas location.
	var flashImage = gridPaper.image(
		"assets/SPRT_SPRITESHEET.png",
	//	critter.getImage().attr('x'), // <--- Initial canvas X position
	0,
	0, // <--- Initial canvas Y position
		FRAME_WIDTH * TOTAL_FRAMES,
		FRAME_HEIGHT
	).attr({
		opacity: 0
	});

    var currentFrame = 0;

    // Set the image to its fixed starting position (defined in the setup) and make it visible
    flashImage.attr({
        opacity: 1,
        // We set the initial clip rect here. It's relative to the image's (100, 100) position.
        // So, the top-left corner of the clip box is at (0, 0) relative to the image element.
		x: DISPLAY_X,
		y: DISPLAY_Y,
        "clip-rect": DISPLAY_X + ", " + DISPLAY_Y + ", " + FRAME_WIDTH + ", " + FRAME_HEIGHT
    });
		if (side == "right") {
			var bbox = flashImage.getBBox();
			var centerX = DISPLAY_X + FRAME_WIDTH / 2;
			var centerY = DISPLAY_Y + FRAME_HEIGHT / 2;
		//	alert(centerX + ', ' + centerY);
			flashImage.transform("S-1,1," + centerX + "," + centerY);
		}
    var animationInterval = setInterval(function() {


        // if (currentFrame > TOTAL_FRAMES) {
        //     clearInterval(animationInterval);
        //     flashImage.animate({ opacity: 0 }, 100, function() {
        //         this.hide();
        //     });
        //     return;
        // }

        // --- THE CORRECTED CLIP CALCULATION ---

        // CLIP_OFFSET_X is the horizontal distance from the left edge of the sprite sheet
        // to the start of the current frame.
        var CLIP_OFFSET_X = currentFrame * FRAME_WIDTH;
		var newImageX = DISPLAY_X - CLIP_OFFSET_X;
        // The clip-rect uses these element-relative coordinates
        flashImage.attr({
            // clip-rect format: "clip-x, clip-y, clip-width, clip-height"
  //          "clip-rect": CLIP_OFFSET_X + ", 0, " + FRAME_WIDTH + ", " + FRAME_HEIGHT,
		 	x: newImageX
        });
		if (side == "right") {
			var bbox = flashImage.getBBox();
			var centerX = DISPLAY_X + FRAME_WIDTH / 2;
			var centerY = DISPLAY_Y + FRAME_HEIGHT / 2;
		//	alert(centerX + ', ' + centerY);
			flashImage.transform("S-1,1," + centerX + "," + centerY);
		}
	//	flashImage.transform('t' + (-CLIP_OFFSET_X) - ',' + 0);

        currentFrame++;
    }, ANIMATION_SPEED_MS);
}

function triggerAttackAnimation(critter, side) {
	var ANIMATION_SPEED_MS = 120;
	var DISPLAY_X = critter.getImage().attr('x');
	var DISPLAY_Y = critter.getImage().attr('y');

	var FRAME_WIDTH = 200;

	var FRAME_HEIGHT = 200;
	var TOTAL_FRAMES = 4;
	// Run 12.5 frames per second
	// 1. Create the image element on the paper. This is its permanent canvas location.
	var flashImage = gridPaper.image(
		"assets/ATK_SPRITESHEET.png",
	//	critter.getImage().attr('x'), // <--- Initial canvas X position
	0,
	0, // <--- Initial canvas Y position
		FRAME_WIDTH * TOTAL_FRAMES,
		FRAME_HEIGHT
	).attr({
		opacity: 0
	});

    var currentFrame = 0;

    // Set the image to its fixed starting position (defined in the setup) and make it visible
    flashImage.attr({
        opacity: 1,
        // We set the initial clip rect here. It's relative to the image's (100, 100) position.
        // So, the top-left corner of the clip box is at (0, 0) relative to the image element.
		x: DISPLAY_X,
		y: DISPLAY_Y,
        "clip-rect": DISPLAY_X + ", " + DISPLAY_Y + ", " + FRAME_WIDTH + ", " + FRAME_HEIGHT
    });
		if (side == "right") {
			var bbox = flashImage.getBBox();
			var centerX = DISPLAY_X + FRAME_WIDTH / 2;
			var centerY = DISPLAY_Y + FRAME_HEIGHT / 2;
		//	alert(centerX + ', ' + centerY);
			flashImage.transform("S-1,1," + centerX + "," + centerY);
		}
    var animationInterval = setInterval(function() {


        // if (currentFrame > TOTAL_FRAMES) {
        //     clearInterval(animationInterval);
        //     flashImage.animate({ opacity: 0 }, 100, function() {
        //         this.hide();
        //     });
        //     return;
        // }

        // --- THE CORRECTED CLIP CALCULATION ---

        // CLIP_OFFSET_X is the horizontal distance from the left edge of the sprite sheet
        // to the start of the current frame.
        var CLIP_OFFSET_X = currentFrame * FRAME_WIDTH;
		var newImageX = DISPLAY_X - CLIP_OFFSET_X;
        // The clip-rect uses these element-relative coordinates
        flashImage.attr({
            // clip-rect format: "clip-x, clip-y, clip-width, clip-height"
  //          "clip-rect": CLIP_OFFSET_X + ", 0, " + FRAME_WIDTH + ", " + FRAME_HEIGHT,
		 	x: newImageX
        });
		if (side == "right") {
			var bbox = flashImage.getBBox();
			var centerX = DISPLAY_X + FRAME_WIDTH / 2;
			var centerY = DISPLAY_Y + FRAME_HEIGHT / 2;
		//	alert(centerX + ', ' + centerY);
			flashImage.transform("S-1,1," + centerX + "," + centerY);
		}
	//	flashImage.transform('t' + (-CLIP_OFFSET_X) - ',' + 0);

        currentFrame++;
    }, ANIMATION_SPEED_MS);
}

function triggerSupportAnimation(critter, side) {
	var ANIMATION_SPEED_MS = 90;
	var DISPLAY_X = critter.getImage().attr('x');
	var DISPLAY_Y = critter.getImage().attr('y');

	var FRAME_WIDTH = 200;

	var FRAME_HEIGHT = 200;
	var TOTAL_FRAMES = 5;
	// Run 12.5 frames per second
	// 1. Create the image element on the paper. This is its permanent canvas location.
	var flashImage = gridPaper.image(
		"assets/SPRT_SPRITESHEET.png",
	//	critter.getImage().attr('x'), // <--- Initial canvas X position
	0,
	0, // <--- Initial canvas Y position
		FRAME_WIDTH * TOTAL_FRAMES,
		FRAME_HEIGHT
	).attr({
		opacity: 0
	});

    var currentFrame = 0;

    // Set the image to its fixed starting position (defined in the setup) and make it visible
    flashImage.attr({
        opacity: 1,
        // We set the initial clip rect here. It's relative to the image's (100, 100) position.
        // So, the top-left corner of the clip box is at (0, 0) relative to the image element.
		x: DISPLAY_X,
		y: DISPLAY_Y,
        "clip-rect": DISPLAY_X + ", " + DISPLAY_Y + ", " + FRAME_WIDTH + ", " + FRAME_HEIGHT
    });
		if (side == "right") {
			var bbox = flashImage.getBBox();
			var centerX = DISPLAY_X + FRAME_WIDTH / 2;
			var centerY = DISPLAY_Y + FRAME_HEIGHT / 2;
		//	alert(centerX + ', ' + centerY);
			flashImage.transform("S-1,1," + centerX + "," + centerY);
		}
    var animationInterval = setInterval(function() {


        // if (currentFrame > TOTAL_FRAMES) {
        //     clearInterval(animationInterval);
        //     flashImage.animate({ opacity: 0 }, 100, function() {
        //         this.hide();
        //     });
        //     return;
        // }

        // --- THE CORRECTED CLIP CALCULATION ---

        // CLIP_OFFSET_X is the horizontal distance from the left edge of the sprite sheet
        // to the start of the current frame.
        var CLIP_OFFSET_X = currentFrame * FRAME_WIDTH;
		var newImageX = DISPLAY_X - CLIP_OFFSET_X;
        // The clip-rect uses these element-relative coordinates
        flashImage.attr({
            // clip-rect format: "clip-x, clip-y, clip-width, clip-height"
  //          "clip-rect": CLIP_OFFSET_X + ", 0, " + FRAME_WIDTH + ", " + FRAME_HEIGHT,
		 	x: newImageX
        });
		if (side == "right") {
			var bbox = flashImage.getBBox();
			var centerX = DISPLAY_X + FRAME_WIDTH / 2;
			var centerY = DISPLAY_Y + FRAME_HEIGHT / 2;
		//	alert(centerX + ', ' + centerY);
			flashImage.transform("S-1,1," + centerX + "," + centerY);
		}
	//	flashImage.transform('t' + (-CLIP_OFFSET_X) - ',' + 0);

        currentFrame++;
    }, ANIMATION_SPEED_MS);
}

function triggerBlockAnimation(critter, side) {
	var ANIMATION_SPEED_MS = 90;
	var DISPLAY_X = critter.getImage().attr('x');
	var DISPLAY_Y = critter.getImage().attr('y');

	var FRAME_WIDTH = 200;

	var FRAME_HEIGHT = 200;
	var TOTAL_FRAMES = 5;
	// Run 12.5 frames per second
	// 1. Create the image element on the paper. This is its permanent canvas location.
	var flashImage = gridPaper.image(
		"assets/DEF_SPRITESHEET.png",
	//	critter.getImage().attr('x'), // <--- Initial canvas X position
	0,
	0, // <--- Initial canvas Y position
		FRAME_WIDTH * TOTAL_FRAMES,
		FRAME_HEIGHT
	).attr({
		opacity: 0
	});

    var currentFrame = 0;

    // Set the image to its fixed starting position (defined in the setup) and make it visible
    flashImage.attr({
        opacity: 1,
        // We set the initial clip rect here. It's relative to the image's (100, 100) position.
        // So, the top-left corner of the clip box is at (0, 0) relative to the image element.
		x: DISPLAY_X,
		y: DISPLAY_Y,
        "clip-rect": DISPLAY_X + ", " + DISPLAY_Y + ", " + FRAME_WIDTH + ", " + FRAME_HEIGHT
    });
		if (side == "right") {
			var bbox = flashImage.getBBox();
			var centerX = DISPLAY_X + FRAME_WIDTH / 2;
			var centerY = DISPLAY_Y + FRAME_HEIGHT / 2;
		//	alert(centerX + ', ' + centerY);
			flashImage.transform("S-1,1," + centerX + "," + centerY);
		}
    var animationInterval = setInterval(function() {


        // if (currentFrame > TOTAL_FRAMES) {
        //     clearInterval(animationInterval);
        //     flashImage.animate({ opacity: 0 }, 100, function() {
        //         this.hide();
        //     });
        //     return;
        // }

        // --- THE CORRECTED CLIP CALCULATION ---

        // CLIP_OFFSET_X is the horizontal distance from the left edge of the sprite sheet
        // to the start of the current frame.
        var CLIP_OFFSET_X = currentFrame * FRAME_WIDTH;
		var newImageX = DISPLAY_X - CLIP_OFFSET_X;
        // The clip-rect uses these element-relative coordinates
        flashImage.attr({
            // clip-rect format: "clip-x, clip-y, clip-width, clip-height"
  //          "clip-rect": CLIP_OFFSET_X + ", 0, " + FRAME_WIDTH + ", " + FRAME_HEIGHT,
		 	x: newImageX
        });
		if (side == "right") {
			var bbox = flashImage.getBBox();
			var centerX = DISPLAY_X + FRAME_WIDTH / 2;
			var centerY = DISPLAY_Y + FRAME_HEIGHT / 2;
		//	alert(centerX + ', ' + centerY);
			flashImage.transform("S-1,1," + centerX + "," + centerY);
		}
	//	flashImage.transform('t' + (-CLIP_OFFSET_X) - ',' + 0);

        currentFrame++;
    }, ANIMATION_SPEED_MS);
}

var timeout = null;
function animate(str) {
//	triggerAttackAnimation();
	var bigTokens = str.split("|");
    for (var i = 0; i < bigTokens.length; i++) {
        var smallTokens = bigTokens[i].split(",");
      if (smallTokens[1] == "stealth") {
      	  var critter = identifyCritter(smallTokens[2], smallTokens[3]);
		  timeout = setTimeout(function() {
          critter.getImages().attr('opacity',0.3);
          critter.getImage().attr('opacity',0.3);

          },smallTokens[4] * 1000);
    	} else if (smallTokens[1] == "unstealth") {
    	  var critter = identifyCritter(smallTokens[2], smallTokens[3]);
    	  timeout = setTimeout(function() {
          critter.getImages().attr('opacity',1);
          critter.getImage().attr('opacity',1);
          },smallTokens[4] * 1000);
        } else if (smallTokens[1] == "basic") {
    		var critter = identifyCritter(smallTokens[2], smallTokens[3]);
			var side = smallTokens[3];
            timeout = setTimeout(function() {
        	critter.getAltImage().animate({y: critter.getY() - 20},50, function() {
        																			critter.getAltImage().animate({y: critter.getY()} , 50)
        																			critter.getImage().animate({y: critter.getY()} , 50)
        																			});
        	critter.getImage().animate({y: critter.getY() - 20},50);
		//	triggerAttackAnimation(critter, side);
			},smallTokens[4] * 1000 - 100);
        } else if (smallTokens[1] == "cancel") {
            var critter = identifyCritter(smallTokens[2], smallTokens[3]);
        	critter.getImage().stop();
        	critter.getAltImage().stop();
        	if (timeout != null) {
        		window.clearTimeout(timeout);
        	}

        } else if (smallTokens[1] == "action") {
		//	alert(str);
		console.log(str);
			var side = smallTokens[3];
			var target;
			if (smallTokens[5] == "spot"){
				target = identifySpot(smallTokens[2]);
			} else {
				target = identifyCritter(smallTokens[2], smallTokens[3]);
			}
		//	timeout = setTimeout(function() {
		console.log(target);
			triggerActionAnimation(smallTokens[4], smallTokens[5], target, side);
		//	},smallTokens[4] * 1000 - 50);
		}
     }
}
function gameOver(str) {
	var tokens = str.split(',');
	GAMEOVER = true;
	locked = true;
    log.style.height = HEIGHT - 450 + "px";
    logBackground.attr({"clip-rect": (logBackground.attr('x') + " " + logBackground.attr('y') + " " + logBackground.attr('width') + " " + logBackground.attr('height'))});
    infoBoxBackground.attr({"clip-rect": (infoBoxBackground.attr('x') + " " + infoBoxBackground.attr('y') + " " + infoBoxBackground.attr('width') + " " + infoBoxBackground.attr('height'))});
    infoBox.move(infoBox.getY() - 110);
    infoBox.setFontSize(13.5);
   	log.style.top = topPos + "px";

        	document.getElementById("usermsg").style.visibility="visible";
	readyButton.enable();
    readyButton.show();
	while (icons.length > 0) {
        icons.pop().remove();
    }
    items.forEach(function(item) {
    	if (item.isQueued()) {
    		item.getMiniPic().remove();
    		item.getLine().remove();
    		item.getPic().remove();
    	}
    });
	upperRect.hide(); lowerRect.hide(); bar.hide();
	if (tokens[1] == "win") {
		readyButton.getRect()[1].attr('text','YOU WIN!');
	} else if (tokens[1] == "loss") {
		readyButton.getRect()[1].attr('text','YOU LOSE');
	} else if (tokens[1] == "draw") {
		readyButton.getRect()[1].attr('text','DRAW');
	}
}



function reset() {
    while (icons.length > 0) {
        icons.pop().remove();
    }
    items.forEach(function(item) {
    	if (item.isQueued()) {
    		item.getMiniPic().remove();
    		item.getLine().remove();
    		item.getPic().remove();
    	}
    });

   	beforeClick = true;

	critters.forEach(function(f) {
		f.coordinateImages();

	});
	upperRect.hide(); lowerRect.hide();
    locked = false;
    initialCalcQueue = true;
}
var beforeClick = true;
var isMyPaper = true;
var opponentPaper = gridPaper.image("assets/ability/opponentabilitypaper.png", WIDTH/2 - 257, 423, 514, 150).hide();
var myPaper = gridPaper.image("assets/ability/abilitypaper.png", WIDTH/2 - 257, 423, 514, 150).hide();
function displayAbilities(str) {
	if (str == "remove") {
		while(abilities.length > 0) {
			abilities.pop().remove();
		}

	} else {

	while(abilities.length > 0) {
		abilities.pop().remove();
	}
    var bigTokens = str.split("|");

    if (bigTokens[0].split(",")[0] == "clickable") {
    	if (!isMyPaper || beforeClick) {
    		opponentPaper.hide();
    		myPaper.show();
    		isMyPaper = true;
    		beforeClick = false;
    	}
    } else {
    	if (isMyPaper || beforeClick) {
        	opponentPaper.show();
    		myPaper.hide();
    		isMyPaper = false;
    		beforeClick = false;
        }
    }

    for (var i = 0; i < bigTokens.length -1; i++) {
        var smallTokens = bigTokens[i].split(",");
        var ability = new Ability(smallTokens[0], smallTokens[1], smallTokens[2], smallTokens[3], smallTokens[4], smallTokens[5], smallTokens[6], smallTokens[7], smallTokens[8]);
        abilities.push(ability);
    }

    abilities.forEach(function(a) {
        a.getPic().abilityClickable(a);
        a.getPic().abilityHoverable(a);
    });
    }

}


function changeBars(str) {
    var bigTokens = str.split("|");
    for (var i = 0; i < bigTokens.length; i++) {
        var smallTokens = bigTokens[i].split(",");
		var critter = identifyCritter(smallTokens[1], smallTokens[2]);
		var value = parseInt(smallTokens[3]);
      	if (smallTokens[0] == "health") {
        	critter.setHealth(value);
        }
      	else if (smallTokens[0] == "energy") {
        	critter.setEnergy(value);
       	}
    }
}
var lastBigTokenLen = 0;
function indicateActions(str) {
	var massiveTokens = str.split("~");
    var bigTokens = massiveTokens[0].split("|");
	if (bigTokens.length == lastBigTokenLen + 1) {

		bigTokens = massiveTokens[1].split("|");
	}
    for (var i = 1; i < bigTokens.length; i++) {
    	var smallTokens = bigTokens[i].split(",");
    	if (smallTokens[0] == "move") {
        	identifySpot(smallTokens[1]).indicateMove();
    	} else if (smallTokens[0] == "attack") {
        	identifyCritter(smallTokens[1], smallTokens[2]).indicateAttack();
    	} else if (smallTokens[0] == "support") {
    		identifyCritter(smallTokens[1], smallTokens[2]).indicateSupport();
    	} else if (smallTokens[0] == "block") {
        	identifyCritter(smallTokens[1], smallTokens[2]).indicateBlock();
    	} else if (smallTokens[0] == "attackspot") {
        	identifySpot(smallTokens[1]).indicateAttack();
    	} else if (smallTokens[0] == "supportspot") {
        	identifySpot(smallTokens[1]).indicateSupport();
    	}
    }
    lastBigTokenLen = massiveTokens[0].length;
}


function displayActionQueue(str) {
	if (!calculating) {

   		while (icons.length > 0) {

        	icons.pop().remove();
    	}
    }

    var bigTokens = str.split("_")
    var lastX = 130;
	if (bigTokens[1] == "remove") {
	   		while (icons.length > 0) {

        	icons.pop().remove();
    	}
	} else {

    for (var i = 1; i < bigTokens.length -1; i++) {
        var smallTokens = bigTokens[i].split("/");
		var anIcon = null;

        if (bigTokens[bigTokens.length -1] == iconY + 100 && smallTokens.length > 3) {
        	anIcon = new icon(smallTokens[0], parseInt(smallTokens[1]), smallTokens[2], smallTokens[3], smallTokens[4], lastX, smallTokens[5], smallTokens[6], smallTokens[7], smallTokens[8], bigTokens[bigTokens.length-1]);
        	initialCalcQueue = false;
        } else if (smallTokens.length > 3) {
      		anIcon = new icon(smallTokens[0], parseInt(smallTokens[1]), smallTokens[2], smallTokens[3], smallTokens[4], lastX, smallTokens[5], smallTokens[6], smallTokens[7], smallTokens[8]);
        }
        var lastX;
		if (anIcon != null) {
        	icons.push(anIcon);
        	lastX = anIcon.getX2();
        }

     }
    icons.forEach(function(m){
        m.getIcon().iconDraggable(m,icons);
        m.getIcon().iconHoverable(m);
    });

}
}

Raphael.st.abilityHoverable = function(ability) {
	var me = this,


	hoverIn = function() {
	infoBox.setInfo(ability.getDescription());
	me[0].attr('fill-opacity', 0.5);
	me.toFront();
	if (isMyPaper) {
		document.body.style.cursor = 'pointer';
	}
	},
	hoverOut = function() {
	infoBox.setInfo('');
	if (!ability.isClicked()) {
		me[0].attr('fill-opacity', 0);
	}
	if (isMyPaper) {
			document.body.style.cursor = 'auto';
	}
	};
	this.hover(hoverIn, hoverOut);

}

Raphael.st.abilityClickable = function(ability) {
    var me = this,
    onClick = function() {
        if (ability.isClickable()) {
            ws.send("click,ability,ability," + ability.getName() + "," + id);
        }
    };
    this.click(onClick);
};



Raphael.st.iconHoverable = function(icon) {
    var me = this,
    hoverIn = function() {
    if (!iconDragging) {
    	if (!calculating) {
    		critters.forEach(function(f) {f.unindicateAttack(); f.unindicateSupport(); f.unindicateBlock();});
			spots.forEach(function(s) {s.unindicateMove(); s.unindicateAttack(); s.unindicateSupport();});
    		icon.indicate();
    		document.body.style.cursor = 'pointer';
    	}
    	infoBox.setInfo(icon.getInfo());
    	}
    					  },
    hoverOut = function() {
    if (!iconDragging) {
    	document.body.style.cursor = 'auto';
		infoBox.setInfo("");
        	if (!locked) {

    	indicateActions(actionQueueStr);
    	if (!abilitySelected) {
    		showPossibleMoves(possibleMovesStr, true);
    	}

    	}
	}
    };

    this.hover(hoverIn, hoverOut);

};

var itemY = actionBarFrame.attr('y') + 82;
Raphael.st.itemDraggable = function(x,y,image,item) {
	var me = this,
	lx = 0,
	ly = 0,
	origX = x,
	origY = y,
	pos,
	line,
	moveFnc = function(dx, dy) {
	if (!initialPhase && !locked) {
		lx = dx;
		ly = dy;
		item.getPic().transform('t' + lx + ',' + ly);
		}
	},
	startFnc = function() {

	if (!initialPhase && !locked) {
		if (item.isQueued()) {

			item.getLine().remove();
		}
		item.getPic().toFront();
		item.getMiniImage().hide();
		itemBeingDragged = true;
		item.getPic()[0].hide();
		item.getPic()[1].show();

		}
	},
	endFnc = function() {
		if (!initialPhase && !locked) {
		itemBeingDragged = false;
		var initialY = image.attr('y');
		var initialX = image.attr('x');
	    var midX = initialX + lx + image.attr('width')/2;
		var currX = lx + origX;
		var lineX = 0;
		if (ly + initialY >= 316  && ly + initialY <= 444) {

			item.setQueued(true);
			item.getPic()[0].hide();
			item.getPic()[1].hide();
			item.getMiniImage().show().toFront();

			var leftX = 130;

			item.getPic().transform('t' + (-lx) - ',' + (-ly));

			if (midX <= 130) {
				var newX = 130;
				var newNewX = newX;

				itemsChosen.forEach(function(it) {
					if (it.getPic()[0] != null) {
						if (it.getMiniImage() == newX && it != item) {
							itemsChosen.forEach(function(ite) {
								if (ite.getMiniImage().attr('x') == newX + 30 && ite != item) {
									newNewX += 30;
								}
							});
							newNewX += 30;
						}
					}
				});

			    item.getMiniImage().attr('x', newNewX).attr('y', itemY);
			    item.getPic().attr('x', newNewX).attr('y', itemY);
			    lineX = newX;
				if (item.isQueued()) {
					ws.send("click,item,remove," + item.getName() + "," + 0 + "," + id);
				}
				ws.send("click,item,add," + item.getName() + "," +  0 + "," + id);

			} else if (midX >= WIDTH - 130) {
				var newX = WIDTH-130;
				var newNewX = newX;

				itemsChosen.forEach(function(it) {
					if (it.getPic()[0] != null) {
						if (it.getPic()[0].attr('x') == newX && it != item) {
							itemsChosen.forEach(function(ite) {
								if (ite.getMiniImage().attr('x') == newX + 30 && ite != item) {
									newNewX += 30;
								}
							});
							newNewX += 30;
						}
					}
				});

			  	item.getMiniImage().attr('x', newNewX).attr('y', itemY);
			  	item.getPic().attr('x', newNewX).attr('y', itemY);
			  	lineX = newX;
				if (item.isQueued()) {
					ws.send("click,item,remove," + item.getName() + "," + 10 + "," + id);
				}
				ws.send("click,item,add," + item.getName() + "," +  10 + "," + id);
			} else {

				for (var i = 0; i < 11; i++) {
					if (midX <= leftX) {
						var newX;
						if (leftX - midX <= midX - (leftX - 0.1*(WIDTH-260))) {
							newX = leftX;
							if (item.isQueued()) {
							   ws.send("click,item,remove," + item.getName() + "," + pos + "," + id);
							}
							ws.send("click,item,add," + item.getName() + "," +  (i) + "," + id);

							pos = i;
						} else {
							newX = leftX - 0.1*(WIDTH-260);


							if (item.isQueued()) {
							   ws.send("click,item,remove," + item.getName() + "," + pos + "," + id);
							}
							ws.send("click,item,add," + item.getName() + "," +  (i -1) + "," + id);
							pos = i-1;
						}
						var newNewX = newX;

						itemsChosen.forEach(function(it) {
						if (it.getPic()[0] != null) {

								if (it.getMiniImage().attr('x') == newX && it != item) {

									itemsChosen.forEach(function(ite) {
										if (ite.getMiniImage().attr('x') == newX + 30 && ite != item) {
											newNewX += 30;
										}
									});
									newNewX += 30;
								}
								}
							});

						item.getMiniImage().attr('x', newNewX).attr('y', itemY);
						item.getPic().attr('x', newNewX).attr('y', itemY);


						lineX = newX;
						break;
					}
					leftX += 0.1*(WIDTH-260);
				}
			}
			line = gridPaper.path("M" + (lineX) + " " + iconY + " L " + (lineX)  + " " + (itemY + 25)).attr('stroke', 'white').attr('stroke-width', '1');
			item.setLine(line);
		} else {
			item.getPic()[0].show();
			item.getPic()[1].show();
			item.getMiniImage().hide();
            item.getPic().transform('t' + (-lx) - ',' + (-ly));
            if (item.isQueued()) {
            	item.setQueued(false);
            	ws.send("click,item,remove," + item.getName() + "," + pos + "," + id);
            	item.getPic().forEach(function(n){
			  		n.attr('x', item.getX()).attr('y', item.getY());
			  	});
			  	item.getMiniImage().attr('x', 0);
	//		  	item.coordinateTextBox();
        	}
        }
        }
        lx = 0;
	};
	this.drag(moveFnc, startFnc, endFnc);
}
var iconDragging = false;


Raphael.st.itemDraggable = function(x,y,image,item) {
	var me = this,
	lx = 0,
	ly = 0,
	origX = x,
	origY = y,
	pos,
	line,
	moveFnc = function(dx, dy) {
	if (!initialPhase && !locked) {
		lx = dx;
		ly = dy;
		item.getPic().transform('t' + lx + ',' + ly);
		}
	},
	startFnc = function() {

	if (!initialPhase && !locked) {
		if (item.isQueued()) {

			item.getLine().remove();
		}
		item.getPic().toFront();
		item.getMiniImage().hide();
		itemBeingDragged = true;
		item.getPic()[0].hide();
		item.getPic()[1].show();

		}
	},
	endFnc = function() {
		if (!initialPhase && !locked) {
		itemBeingDragged = false;
		var initialY = image.attr('y');
		var initialX = image.attr('x');
	    var midX = initialX + lx + image.attr('width')/2;
		var currX = lx + origX;
		var lineX = 0;
		if (ly + initialY >= 316  && ly + initialY <= 444) {

			item.setQueued(true);
			item.getPic()[0].hide();
			item.getPic()[1].hide();
			item.getMiniImage().show().toFront();

			var leftX = 130;

			item.getPic().transform('t' + (-lx) - ',' + (-ly));

			if (midX <= 130) {
				var newX = 130;
				var newNewX = newX;

				itemsChosen.forEach(function(it) {
					if (it.getPic()[0] != null) {
						if (it.getMiniImage() == newX && it != item) {
							itemsChosen.forEach(function(ite) {
								if (ite.getMiniImage().attr('x') == newX + 30 && ite != item) {
									newNewX += 30;
								}
							});
							newNewX += 30;
						}
					}
				});

			    item.getMiniImage().attr('x', newNewX).attr('y', itemY);
			    item.getPic().attr('x', newNewX).attr('y', itemY);
			    lineX = newX;
				if (item.isQueued()) {
					ws.send("click,item,remove," + item.getName() + "," + 0 + "," + id);
				}
				ws.send("click,item,add," + item.getName() + "," +  0 + "," + id);

			} else if (midX >= WIDTH - 130) {
				var newX = WIDTH-130;
				var newNewX = newX;

				itemsChosen.forEach(function(it) {
					if (it.getPic()[0] != null) {
						if (it.getPic()[0].attr('x') == newX && it != item) {
							itemsChosen.forEach(function(ite) {
								if (ite.getMiniImage().attr('x') == newX + 30 && ite != item) {
									newNewX += 30;
								}
							});
							newNewX += 30;
						}
					}
				});

			  	item.getMiniImage().attr('x', newNewX).attr('y', itemY);
			  	item.getPic().attr('x', newNewX).attr('y', itemY);
			  	lineX = newX;
				if (item.isQueued()) {
					ws.send("click,item,remove," + item.getName() + "," + 10 + "," + id);
				}
				ws.send("click,item,add," + item.getName() + "," +  10 + "," + id);
			} else {

				for (var i = 0; i < 11; i++) {
					if (midX <= leftX) {
						var newX;
						if (leftX - midX <= midX - (leftX - 0.1*(WIDTH-260))) {
							newX = leftX;
							if (item.isQueued()) {
							   ws.send("click,item,remove," + item.getName() + "," + pos + "," + id);
							}
							ws.send("click,item,add," + item.getName() + "," +  (i) + "," + id);

							pos = i;
						} else {
							newX = leftX - 0.1*(WIDTH-260);


							if (item.isQueued()) {
							   ws.send("click,item,remove," + item.getName() + "," + pos + "," + id);
							}
							ws.send("click,item,add," + item.getName() + "," +  (i -1) + "," + id);
							pos = i-1;
						}
						var newNewX = newX;

						itemsChosen.forEach(function(it) {
						if (it.getPic()[0] != null) {

								if (it.getMiniImage().attr('x') == newX && it != item) {

									itemsChosen.forEach(function(ite) {
										if (ite.getMiniImage().attr('x') == newX + 30 && ite != item) {
											newNewX += 30;
										}
									});
									newNewX += 30;
								}
								}
							});

						item.getMiniImage().attr('x', newNewX).attr('y', itemY);
						item.getPic().attr('x', newNewX).attr('y', itemY);


						lineX = newX;
						break;
					}
					leftX += 0.1*(WIDTH-260);
				}
			}
			line = gridPaper.path("M" + (lineX) + " " + iconY + " L " + (lineX)  + " " + (itemY + 25)).attr('stroke', 'white').attr('stroke-width', '1');
			item.setLine(line);
		} else {
			item.getPic()[0].show();
			item.getPic()[1].show();
			item.getMiniImage().hide();
            item.getPic().transform('t' + (-lx) - ',' + (-ly));
            if (item.isQueued()) {
            	item.setQueued(false);
            	ws.send("click,item,remove," + item.getName() + "," + pos + "," + id);
            	item.getPic().forEach(function(n){
			  		n.attr('x', item.getX()).attr('y', item.getY());
			  	});
			  	item.getMiniImage().attr('x', 0);
	//		  	item.coordinateTextBox();
        	}
        }
        }
        lx = 0;
	};
	this.drag(moveFnc, startFnc, endFnc);
}
var iconDragging = false;
Raphael.st.iconDraggable = function(icon, arr) {

  var me = this,
      lx = 0,
      ly = 0,
      collapsed = false,
      changed = false,
      origPos = 0,
      iconPos = -2,
      newIconPos = 0,
      origIconX = 0;

      moveFnc = function(dx, dy) {
      if (!locked) {

          me.toFront();
          lx = dx
          ly = dy
          if ((ly < -70 || ly > 70) && !collapsed) {

            iconPos = arr.indexOf(icon);
            arr.splice(iconPos, 1);

            collapsed = true;

            for (var i = 0; i < icons.length; i++) {
              if (i == 0) {
                    icons[i].setX(130);
                    if (icons[i] != icon) {
                        slide(icons[i].getX(), icons[i]);
                    ;}
              } else {
                    icons[i].setX(icons[i -1 ].getX2());
                    if (icons[i] != icon) {
                        slide(icons[i].getX(), icons[i]);
                    }
                }
            }
          } else if (collapsed && ly > -70 && ly < 70) {
            collapsed = false;
            if (arr.length == 0) {
                arr.splice(0,0,icon);
            } else {

            for (var i = 0; i < arr.length; i++) {

                var iconMidPoint = origIconX + lx + icon.getWidth()/2;

                var prevIconMidPoint = arr[i].getX() + arr[i].getWidth()/2;

                if (arr.length > 1) {
                 var nextIconMidPoint = arr[i+1].getX() + arr[i+1].getWidth()/2;
                }
                if (iconMidPoint > arr[arr.length - 1].getX() + arr[arr.length - 1].getWidth()/2) {
                    arr.splice(arr.length, 0 , icon);
                    newIconPos = arr.length-1;
                     break;
                } else if (iconMidPoint < arr[0].getX() + arr[0].getWidth()/2) {
                    arr.splice(0,0,icon);
                    newIconPos = 0;
                    break;
                } else if (prevIconMidPoint < iconMidPoint && nextIconMidPoint >= iconMidPoint) {
                   arr.splice(i+1,0,icon);
                   newIconPos = i+1;
                   break;
                }


            }
            }

            for (var i = 0; i < icons.length; i++) {
              if (i == 0) {

                icons[i].setX(130);
               if (!icons[i] == icon) {
                   slide(icons[i].getX(), icons[i]);
                }
              } else  {
                    icons[i].setX(icons[i -1 ].getX2());
                   if (!(icons[i] == icon)) {
                        slide(icons[i].getX(), icons[i]);
                    }
                }
            }
          }

          me.transform('t' + lx + ',' + ly);
          var rect = icon.getIcon()[0];
          icon.getIcon()[1].attr({"clip-rect": (rect.attr('x') + " " + rect.attr('y') + " " + rect.attr('width') + " " + rect.attr('height'))});
        }
      },
      startFnc = function() {
      	if (!locked) {
      		me.toFront();
      		hideLogButtonsAndText();
      		icon.getIcon()[0].attr('stroke-width', 1);
        	origIconX = icon.getX();
        	origPos = arr.indexOf(icon);
        	newIconPos = origPos;
        }
      },

      endFnc = function() {
      if (!locked) {
      	  showLogButtonsAndText();
          if (ly < -70 || ly >70) {
            critters.forEach(function(f) {f.unindicateAttack(); f.unindicateSupport(); f.unindicateBlock();});
			spots.forEach(function(s) {s.unindicateMove(); s.unindicateAttack(); s.unindicateSupport();});
            ws.send("click,actionqueue,remove," + iconPos + "," + iconPos + "," + id);

            icon.remove();
          } else {
			icon.getIcon()[0].attr('stroke-width', 0);
			icon.moveIcon(icon.getX() + lx - (icon.getX() - origIconX), icon.getY()  + ly);
            me.transform('t' + (-lx) - ',' + (-ly));
            icon.getIcon().insertAfter(backgroundBottom);
            icon.getIcon()[5].toFront();
            if ((origPos != newIconPos) && !collapsed) {
                ws.send("click,actionqueue,switch," + origPos + "," + newIconPos + "," + id);
            }
            slide(icon.getX(), icon);
          }
         changed = false;
         lx = 0;
         ly = 0;
         }

      };
    this.drag(moveFnc, startFnc, endFnc);
};

function slide(newX, icon) {
  var nameY = iconY + 12;
  var descY = iconY + 32;
  var statY = iconY + 54;

  var rectangle = icon.getIcon()[0];
  var slideDiff = newX - rectangle.attr('x');
  var texture = icon.getIcon()[1];
  var newTextureX = texture.attr('x') + slideDiff;
  var rect = true;
  rectangle.animate({x: newX, y: iconY}, 50);
  texture.animate({x: newTextureX, y: iconY}, 50);
  texture.animate({"clip-rect": (newX + " " + iconY + " " + rectangle.attr('width') + " " + rectangle.attr('height'))});
  icon.getIcon()[2].animate({x: newX + icon.getWidth()/2, y: nameY}, 50);
  icon.getIcon()[3].animate({x: newX + icon.getWidth()/2, y: descY}, 50);
  icon.getIcon()[4].animate({x: newX + icon.getWidth()/2, y: statY}, 50);
  icon.getIcon()[5].animate({x: newX, y: iconY}, 50);
}



function Button(rect, text, paper, onclick) {
    this.rect = rect;
    this.text = text;
    this.disabled = true;
    this.disableRect = paper.rect(rect.attr('x'), rect.attr('y'), rect.attr('width'), rect.attr('height'), 5).attr("fill", "gray").attr("fill-opacity", "0.6").hide();
	this.getDisableRect = function(){return this.disableRect};
	this.text.node.setAttribute("class","donthighlight");
	this.isDisabled = function() {return this.disabled;};
	this.hide = function() {this.rect.hide(); this.text.hide(); this.disableRect.hide();}
	this.show = function() {
		if (this.disabled) {
		 	this.disableRect.show();
		} else {
			this.rect.show();
		}
	this.text.show();
	}
    this.getRect = function() {var st = paper.set();
                              st.push(this.rect, this.text, this.disableRect);
	 						return st;}
	this.getRect().toFront();

	this.getRect().hover(
		function() {
			document.body.style.cursor = 'pointer';
		},
		function () {
			document.body.style.cursor = 'auto';
		}
	);
	this.getText = function() {return this.text};
	this.setText = function(newText) {this.text.remove(); this.text = newText; this.text.node.setAttribute("class","donthighlight");};
	this.disable = function() {this.disableRect.toFront(); this.disableRect.show(); this.disabled = true;};
	this.enable = function() {this.disableRect.hide(); this.disabled = false;};
	this.rect.click(onclick);
	this.text.click(onclick);

	this.text.toFront();
	this.text.node.setAttribute("class","donthighlight");
}

function empty() {
}
var readyButton = new Button(gridPaper.rect(WIDTH/2-40, HEIGHT/2 - 92, 80, 34, 3).attr({"fill": "yellow", 'stroke-width': 1.5}),
					gridPaper.text(WIDTH/2, HEIGHT/2 - 75, "Ready").attr("font-family", buttonFont).attr("font-size", 18), gridPaper);
if (!quickStartEnabled) {
	readyButton.disable();
}


readyButton.getRect().click(function () {
	if (initialPhase) {
		if (quickStartEnabled) {
    		var defaultSpotNames = [SIDE + "MiddleBack", SIDE + "BottomFront", SIDE + "TopFront", SIDE + "Bench"];
    		var defaultSpots = [];
    		defaultSpots.push(identifySpot(defaultSpotNames[0]));
    		defaultSpots.push(identifySpot(defaultSpotNames[1]));
    		defaultSpots.push(identifySpot(defaultSpotNames[2]));
    		defaultSpots.push(identifySpot(defaultSpotNames[3]));
    		defaultSpots.forEach(function(ds) {
				ds.setPositionedAt(true);
    		});
    		ws.send("click,initialposition," + SIDE + "," + sideCritters[0].getName() + "," + SIDE + "MiddleBack" + "," +
    												sideCritters[1].getName() + "," + SIDE + "BottomFront" + "," +
    												sideCritters[2].getName() + "," + SIDE + "TopFront" + "," +
    												sideCritters[3].getName() + "," + SIDE + "Bench" + "," + id);
    		locked = true;
    		readyButton.disable();
    		readyButton.getRect()[1].attr('text','waiting');
    	} else {
			ws.send("click,initialposition," + SIDE + "," + sideCritters[0].getName() + "," + sideCritters[0].getToken().getSpot().getName() + "," +
    												sideCritters[1].getName() + "," + sideCritters[1].getToken().getSpot().getName() + "," +
    												sideCritters[2].getName() + "," + sideCritters[2].getToken().getSpot().getName() + "," +
    												sideCritters[3].getName() + "," + sideCritters[3].getToken().getSpot().getName() + "," + id);
    	}
    	readyButton.disable();
    	locked = true;

    } else if (GAMEOVER){
    	window.location.reload(false);
    } else {
        ws.send("click,ready,ready," + id);
        locked = true;
        readyButton.disable();
        readyButton.getRect()[1].attr('text','waiting');
    }
});



function Spot(polygon, name, critterX, critterY) {

    this.polygon = polygon;
    this.polygon.attr("fill", "white").attr("fill-opacity", "0");
    this.getPolygon = function() {return this.polygon}
    this.name = name;
    this.getName = function() {return this.name}
    this.positionedAt = false;
    this.getPositionedAt = function() {return this.positionedAt}
    this.setPositionedAt = function(positionedAt) {this.positionedAt = positionedAt}
    this.actionIndicated = false;
    this.highlighted = false;
    this.isHighlighted = function() {return this.highlighted;}
    this.possibleAttacksShown = false;
    this.possibleSupportsShown = false;
    this.isActionIndicated = function() {return this.actionIndicated}
    this.setActionIndicated = function(actionIndicated) {this.actionIndicated = actionIndicated}
    this.critterX = critterX;
    this.getCritterX =  function() {return this.critterX}
    this.critterY = critterY;
    this.getCritterY = function() {return this.critterY}
    this.compare = function(str) {if(str == name) {return true}return false}

    this.indicateMove = function () {if (!this.highlighted && !this.possibleAttacksShown && !this.possibleSupportsShown){this.polygon.attr ("fill", moveColor).attr("stroke", moveColor).attr ("fill-opacity", 1), this.actionIndicated = true; this.polygon.show();}}
    this.indicateAttack = function () {if (!this.highlighted && !this.possibleAttacksShown && !this.possibleSupportsShown) {this.polygon.attr ("fill", attackColor).attr("stroke", attackColor).attr ("fill-opacity", 1), this.actionIndicated = true; this.polygon.show();}}
    this.indicateSupport = function () {if (!this.highlighted && !this.possibleAttacksShown && !this.possibleSupportsShown) {this.polygon.attr ("fill", supportColor).attr("stroke", supportColor).attr ("fill-opacity", 1), this.actionIndicated = true; this.polygon.show();}}
    this.unindicateMove = function () { if (this.actionIndicated) {this.polygon.attr ("fill-opacity", 0).attr("stroke", "#704000"); this.actionIndicated = false; this.polygon.hide(); return true;} return false;}
    this.unindicateAttack = function () { if (this.actionIndicated) {this.polygon.attr ("fill-opacity", 0).attr("stroke", "#704000"); this.actionIndicated = false; this.polygon.hide(); return true;} return false;}
    this.unindicateSupport = function () { if (this.actionIndicated) {this.polygon.attr ("fill-opacity", 0).attr("stroke", "#704000"); this.actionIndicated = false; this.polygon.hide(); return true;} return false;}

    this.unhighlight = function () { if (this.highlighted) {this.polygon.unhover(); this.polygon.attr ("fill-opacity", 0).attr("stroke-opacity", 0);  this.polygon.insertAfter(background); this.highlighted = false;  this.polygon.hide(); return true;} return false;}
   	this.unPossibleAttacks = function () {if (this.possibleAttacksShown) {this.polygon.unhover(); this.polygon.attr ("fill-opacity", 0); this.polygon.insertAfter(background); this.possibleAttacksShown = false; this.polygon.hide(); return true;} return false;}
    this.unPossibleSupports = function () { if (this.possibleSupportsShown) {this.polygon.unhover(); this.polygon.attr ("fill-opacity", 0); this.polygon.insertAfter(background); this.possibleSupportsShown = false; this.polygon.hide(); return true; } return false;}
}
var upperFrameY = actionBarFrame.attr('y');
var lowerFrameY = upperFrameY + 100;
var upperRect = gridPaper.image("assets/actionbar/actionbarframe.png", 120, upperFrameY, WIDTH - 245, 90).hide();
var lowerRect = gridPaper.image("assets/actionbar/actionbarframe.png", 120, lowerFrameY, WIDTH - 245, 90).hide();
var otherItemY = lowerRect.attr('y') + 82;
var bar = gridPaper.image("assets/actionbar/timebar.png", 120, upperFrameY - 3, (lowerFrameY - upperFrameY + 10 + 90) * 0.29, lowerFrameY - upperFrameY + 5 + 90).hide();
function timeBar () {
	var h = 90;
	var w = WIDTH - 245;
	var startX = 120;
	var startY = 370;
	upperRect.show();
	lowerRect.show();
	abilities.forEach(function(ability) {

		ability.getPic().remove();
	});
//	var bar = gridPaper.path('M' + (startX + 10) + ',' + (startY + 10) + ' L ' + (startX + 10) + ',' + (startY + 80 + h)).attr("stroke-width", "2");
	bar.insertAfter(lowerRect);
	bar.attr('x', startX - 25).show();
	bar.animate({x: w + 70}, 10000, function removeTimeBar() {bar.hide();});


}

function icon (time, energy, user, target, description, x, type, targetSide, indicatorMessage, info, y) {
    this.info = info;
    this.getInfo = function() {return this.info};
    this.description = description;
    this.time = time;
    this.energy = energy;
    this.user = user;
    this.target = target;
    this.width = time/10*(WIDTH-260);
    this.getWidth = function() {return this.width}
    this.y = iconY;
    this.height = 64;
        if (y > 0) {

    	var a = y;
    	a = a+1;
    	a = a-1;
    	a = a/10;
    	this.y = a;


    }
    this.moveIcon = function(x, y) {
    	this.texture.attr({x: this.texture.attr('x') + x - this.rect.attr('x'), y: iconY}, 50);
  		this.texture.attr({"clip-rect": (x + " " + y + " " + this.rect.attr('width') + " " + this.rect.attr('height'))});
    	this.rect.attr('x', x).attr('y', y);


    	this.clickRect.attr('x', x).attr('y', y);
    	this.nameText.attr('x', x + this.width/2).attr('y', y + 12);
    	this.descText.attr('x', x + this.width/2).attr('y', y + 27);
    	this.statText.attr('x', x + this.width/2).attr('y', y + 39);
    };


    this.getY = function() {return this.y}
    this.setY = function(y) { this.y = y}
    this.x = x;
    this.getX = function() {return this.x}
    this.setX = function(x) { this.x = x}
    this.type = type;
    this.getX2 = function() {return (this.x + this.width)}
    this.remove = function() {this.clickRect.remove(); this.rect.remove(); this.nameText.remove(); this.descText.remove(); this.statText.remove(); this.texture.remove();}
    if (this.type == "move") {
         this.rect = gridPaper.rect(this.x, this.y, this.width, this.height).attr("stroke-width", "0").attr("fill", moveColor);
    } else if (this.type == "attack") {
         this.rect = gridPaper.rect(this.x, this.y, this.width, this.height).attr("stroke-width", "0").attr("fill", attackColor);
    } else if (this.type == "support") {
         this.rect = gridPaper.rect(this.x, this.y, this.width, this.height).attr("stroke-width", "0").attr("fill", supportColor);
    } else if (this.type == "block") {
         this.rect = gridPaper.rect(this.x, this.y, this.width, this.height).attr("stroke-width", "0").attr("fill", blockColor);
    }
    this.clickRect = gridPaper.rect(this.x, this.y, this.width, this.height).attr({"stroke-width":0, "fill":"white", "fill-opacity": 0});
    this.getClickRect = function() {return this.clickRect}

//	this.texture = gridPaper.image("assets/actionbar/marbletexture.png", 0,0,1504,1000).attr({"clip-rect": (this.x + " " + this.y + " " + this.width + " " + this.height)});
	this.texture = gridPaper.rect(0,0,0,0);

    this.nameText = gridPaper.text(this.x + this.width/2, this.y + 12, this.user).attr(
  {"font-family":iconFont,
   "font-size":"12"});


    this.descText = gridPaper.text(this.x + this.width/2, this.y + this.height/2, this.description).attr(
  {"font-family":"Caesar Dressing",
   "font-size":"15"});

    this.statText = gridPaper.text(this.x + this.width/2, this.y + this.height - 10, "E:" + this.energy + " T:" + this.time).attr(
  {"font-family":"Caesar Dressing",
   "font-size":"12"});

   this.getUser = function() {return this.user}
   this.getIcon = function() {var st = gridPaper.set();
                              st.push(this.rect, this.texture, this.nameText, this.descText, this.statText, this.clickRect);
                              return st;}
   this.nameText.insertAfter(backgroundBottom);
   this.descText.insertAfter(backgroundBottom);
   this.statText.insertAfter(backgroundBottom);
   this.texture.insertAfter(backgroundBottom);
   this.nameText.node.setAttribute("class","donthighlight");
   this.descText.node.setAttribute("class","donthighlight");
   this.statText.node.setAttribute("class","donthighlight");
   this.rect.insertAfter(backgroundBottom);

   this.indicate = function() {
   showPossibleMoves(",no moves", false);
   showPossibleMoves(",no attacks", false);
   showPossibleMoves(",no supports", false);
   indicateActions(indicatorMessage);
   }


}

function alignTop(t) {
    var b = t.getBBox();
    var h = Math.abs(b.y2) - Math.abs(b.y) + 1;

    t.attr({
        'y': b.y + h
    });
}

function Item (name, description, pos, imageStr) {
	this.name = name;
	this.getName = function() {return this.name;};
	this.queued = false;
	this.isQueued = function() {return this.queued;};
	this.setQueued = function(queued) {this.queued = queued;};
	this.description = description;
	this.getDescription = function() {return description;}
	this.pos = pos;
	this.getPos = function() {return this.pos;};
	this.line = null;
	this.getLine = function() {return this.line};
	this.setLine = function(line) {this.line = line};
	this.y = HEIGHT - 110;
	this.x = WIDTH/2 - 175 + 130 * pos;
	this.getY = function() {return this.y};
	this.setY = function(y) {this.y = y};
	this.getX = function() {return this.x};
	this.setX = function(x) {this.x = x};
	this.width = 80;
	this.height = 80;
	this.itemBox;
	if (pos == 0) {
		this.itemBox = gridPaper.image("assets/items/itembox1.png", this.x, this.y, this.width, this.height);
	} else if (pos == 1) {
		this.itemBox = gridPaper.image("assets/items/itembox2.png", this.x, this.y, this.width, this.height);
	} else {
		this.itemBox = gridPaper.image("assets/items/itembox3.png", this.x, this.y, this.width, this.height);
	}
	this.image = gridPaper.image("assets/items/" + imageStr, this.x, this.y, this.width, this.height);
	this.miniImage = gridPaper.image("assets/items/" + imageStr, 0, 444, 30, 30).hide();
	this.getMiniPic = function (){var st = gridPaper.set();
					   st.push(this.miniImage);
					   return st;};
	this.getMiniImage = function() {return this.miniImage;};
	this.getImage = function() {return this.image;};
	this.getPic = function() {var st = gridPaper.set();
         		  st.push(this.itemBox, this.image);
                  return st;}
}


function Ability (clickable, time, energy, name, description, pos, damage, type, effects) {
    this.clickable;
    if (clickable == "clickable") {
        this.clickable = true;
    } else {
        this.clickable = false;
    }

    this.isClickable = function() {return this.clickable;}
    this.time = time;
    this.energy = energy;
    this.name = name;
    this.getName = function() {return this.name;}
    this.description = description;
    this.getDescription = function(){return this.description;}
    this.width = 115;
    this.x = pos * this.width + WIDTH/2 - this.width*2;
    this.height = 100;
    this.y = 447;
    this.rect = gridPaper.rect(this.x + 5, this.y, this.width - 10, this.height, 3);
    this.rect.attr("stroke-width", 0).attr("fill-opacity", 0);
    this.getRect = function() {return this.rect;}

    this.damage = damage;
    this.type = type;
    this.getType = function() {return this.type;}

    this.effects = effects.split('*');
    this.effectPics = gridPaper.set();
    if (effects != "") {
    	for (var i = 0; i < this.effects.length; i++) {
    		this.effectPics.push(gridPaper.image("assets/EffectSigns/" + this.effects[i], this.x+this.width/2-10 -25*i, this.y+this.height/2 - 10, 20, 20));
   		}
    }

    if (this.clickable) {
   		this.rect.attr("fill", "white");
    } else {
    	this.rect.attr("fill", "#CCE5FF").attr("fill-opacity","0");
    }
    this.setColor = function(color) {this.rect.attr("fill",color);}
    this.clicked = false;
    this.setClicked = function(clicked) {this.clicked = clicked;}
	this.isClicked = function() {return this.clicked;}

	this.nameText = gridPaper.text(this.x + this.width/2,this.y + 10, this.name).attr(
  {"font-family":abilityFont,
   "font-size":"12"});



   this.typeIcon = null;
   if (type == "attack") {
   	 this.typeIcon = gridPaper.image("assets/ability/AttackIcon.png", this.x + this.width/2 - 8, this.y + this.height - 25, 15, 16);
   } else if (type == "block") {
     this.typeIcon = gridPaper.image("assets/ability/BlockIcon.png",this.x + this.width/2 - 8, this.y + this.height - 25, 15, 16);
   } else {
     this.typeIcon = gridPaper.image("assets/ability/SupportIcon.png", this.x + this.width/2 - 8, this.y + this.height - 25, 15, 16);
   }


  //  this.energyIcon = gridPaper.image("assets/ability/manaicon.png", this.x + 40 - 18, this.y + this.height - 33, 10, 10);
//    this.energyText = gridPaper.text(this.x + 27, this.y + this.height - 15, this.energy).attr({"font-family": abilityFont, "font-size": 12});

 //   this.timeIcon = gridPaper.image("assets/ability/timeicon.png", this.x + 103 - 18, this.y + this.height - 33, 10, 10);
//    this.timeText = gridPaper.text(this.x + 90, this.y + this.height - 15, this.time).attr({"font-family": abilityFont, "font-size": 12});


//   this.energyIcon = gridPaper.image("assets/ability/energy.png", this.x + 27 - 18, this.y + this.height - 17, 36, 36);
   this.energyText = gridPaper.text(this.x + 27, this.y + this.height - 15, "E: " + this.energy).attr({"font-family": abilityFont, "font-size": 12});

 //  this.timeIcon = gridPaper.image("assets/ability/timeicon2.png", this.x + 90 - 18, this.y + this.height - 17, 36, 36);
   this.timeText = gridPaper.text(this.x + 90, this.y + this.height - 15, "T: "+ this.time).attr({"font-family": abilityFont, "font-size": 12});





   this.energyText.node.setAttribute("class","donthighlight");
   this.nameText.node.setAttribute("class","donthighlight");
   this.timeText.node.setAttribute("class","donthighlight");



    this.getPic = function() {var st = gridPaper.set();
                              st.push(this.rect, this.nameText, this.descText, this.energyIcon, this.energyText, this.timeIcon, this.timeText, this.typeIcon, this.effectPics);
	 						return st;}

    this.remove = function() {this.getPic().remove();this.effectPics.remove();}

}
var startX = 140;
var startY = HEIGHT/2 - 28;
var horizL = 120;
var vertL = 50;
var h = 45;
var critterSize = 200;
var forwardOffset = 10;

function createPathFromCoords(str) {
	var path = "M ";
	var coords = str.split("-");

	if (coords[0] == "leftBench") {
		path = 'M' + (0) + ',' + (startY - h*3) + 'L' + (startX + vertL*3 - 30) + ',' + (startY - h*3) + 'L' + (startX- 30) + ',' + (startY) + 'L' + (0) + ',' + (startY) + 'L' + (0) + ',' + (startY - h*3);
	} else if (coords[0] == "rightBench") {
		path = 'M' + (WIDTH - startX - vertL*3 + 30) + ',' + (startY - h*3) + 'L' + (WIDTH) + ',' + (startY - h*3) + 'L' + (WIDTH) + ',' + (startY) + 'L' + (WIDTH - startX + 30) + ',' + (startY) + 'L' + (WIDTH - startX - vertL*3 + 30) + ',' + (startY - h*3);

	} else {

	for (var i = 0; i < coords.length; i++) {
		if (coords[i].charAt(0) < 3) {
			path += startX + (3 - coords[i].charAt(1)) * vertL + coords[i].charAt(0) * horizL ;
			path += ',';
			path += startY - (3 - coords[i].charAt(1)) * h;
			if (i != coords.length - 1) {
				path += " L ";
			}
		} else {
			path += WIDTH - (startX + (3 - coords[i].charAt(1)) * vertL + (2 - (coords[i].charAt(0) - 3)) * horizL);
			path += ',';
			path += startY - (3 - coords[i].charAt(1)) * h;
			if (i != coords.length - 1) {
				path += " L ";
			}
		}
	}
	}
	return gridPaper.path(path).attr('stroke-width', '1').attr("fill", "yellow").attr("fill-opacity", 0).attr("stroke", "#704000");

}




var topCritterY = (startY - h*3) - critterSize + 40;
var midCritterY = (startY - h*2) - critterSize + 40;
var botCritterY = (startY - h) - critterSize + 40;
var benchCritterY = (startY - h) - critterSize + 20;


var leftTopBackSpot = new Spot(gridPaper.path('M ' +  (startX + vertL*3) + ',' +  (startY - h*3) + ' L ' +  (startX + vertL*3 + horizL) + ',' +  (startY - h*3) + ' L ' +  (startX + vertL*2 + horizL) + ',' +  (startY - h*2) + ' L ' +  (startX + vertL*2) + ',' +  (startY - h*2) + ' L ' +  (startX + vertL*3) + ',' +  (startY - h*3)  ).attr ("stroke-width", "3"),
                      "leftTopBack",
                      (startX + vertL*3) + (- vertL + horizL - critterSize) / 2 + forwardOffset,
                      topCritterY);

var leftTopFrontSpot = new Spot(gridPaper.path('M ' +  (startX + vertL*3 + horizL) + ',' +  (startY - h*3) + ' L ' +  (startX + vertL*3 + horizL*2) + ',' +  (startY - h*3) + ' L ' +  (startX + vertL*2 + horizL*2) + ',' +  (startY - h*2) + ' L ' +  (startX + vertL*2 + horizL) + ',' +  (startY - h*2) + ' L ' +  (startX + vertL*3 + horizL) + ',' +  (startY - h*3)  ).attr ("stroke-width", "3"),
                      "leftTopFront",
                      startX + vertL*3 + horizL + (- vertL + horizL - critterSize) / 2 + forwardOffset,
                      topCritterY);

var leftMiddleBackSpot = new Spot(gridPaper.path('M ' +  (startX + vertL*2) + ',' +  (startY - h*2) + ' L ' +  (startX + vertL*2 + horizL) + ',' +  (startY - h*2) + ' L ' +  (startX + vertL + horizL) + ',' +  (startY - h) + ' L ' +  (startX + vertL) + ',' +  (startY - h) + ' L ' +  (startX + vertL*2) + ',' +  (startY - h*2) ).attr ("stroke-width", "3"),
                      "leftMiddleBack",
                      startX + vertL*2 + (- vertL + horizL - critterSize) / 2 + forwardOffset,
                      midCritterY);

 var leftMiddleFrontSpot = new Spot(gridPaper.path('M ' +  (startX + vertL*2 + horizL) + ',' +  (startY - h*2) + ' L ' +  (startX + vertL*2 + horizL*2) + ',' +  (startY - h*2) + ' L ' +  (startX + vertL + horizL*2) + ',' +  (startY - h) + ' L ' +  (startX + vertL + horizL) + ',' +  (startY - h) + ' L ' +  (startX + vertL*2 + horizL) + ',' +  (startY - h*2) ).attr ("stroke-width", "3"),
                      "leftMiddleFront",
                      startX + vertL*2 + horizL + (- vertL + horizL - critterSize) / 2 + forwardOffset,
                      midCritterY);

var leftBottomBackSpot = new Spot(gridPaper.path('M' +  (startX + vertL) + ',' +  (startY - h) + ' L ' +  (startX + vertL + horizL) + ',' +  (startY - h) + ' L ' +  (startX + horizL) + ',' +  (startY) + ' L ' +  (startX) + ',' +  (startY) + ' L ' +  (startX + vertL) + ',' +  (startY - h)).attr ("stroke-width", "3"),
                      "leftBottomBack",
                      startX + vertL + (- vertL + horizL - critterSize) / 2 + forwardOffset,
                      botCritterY);

var leftBottomFrontSpot = new Spot(gridPaper.path('M' +  (startX + vertL + horizL) + ',' +  (startY - h) + ' L ' +  (startX + vertL + horizL*2) + ',' +  (startY - h) + ' L ' +  (startX + horizL*2) + ',' +  (startY) + ' L ' +  (startX + horizL) + ',' +  (startY) + ' L ' +  (startX + vertL + horizL) + ',' +  (startY - h)).attr ("stroke-width", "3"),
                      "leftBottomFront",
                      startX + vertL + horizL + (- vertL + horizL - critterSize) / 2 + forwardOffset,
                      botCritterY);


var rightTopBackSpot = new Spot(gridPaper.path('M ' +  (WIDTH - startX - vertL*3) + ',' +  (startY - h*3) + ' L ' +  (WIDTH - startX - vertL*3 - horizL) + ',' +  (startY - h*3) + ' L ' +  (WIDTH - startX - vertL*2 - horizL) + ',' +  (startY - h*2) + ' L ' +  (WIDTH - startX - vertL*2) + ',' +  (startY - h*2) + ' L ' +  (WIDTH - startX - vertL*3) + ',' +  (startY - h*3)  ).attr ("stroke-width", "3"),
                      "rightTopBack",
                      WIDTH - startX - vertL*3 + (vertL - horizL - critterSize) / 2 - forwardOffset,
                      topCritterY);

var rightTopFrontSpot = new Spot(gridPaper.path('M ' +  (WIDTH - startX - vertL*3 - horizL) + ',' +  (startY - h*3) + ' L ' +  (WIDTH - startX - vertL*3 - horizL*2) + ',' +  (startY - h*3) + ' L ' +  (WIDTH - startX - vertL*2 - horizL*2) + ',' +  (startY - h*2) + ' L ' +  (WIDTH - startX - vertL*2 - horizL) + ',' +  (startY - h*2) + ' L ' +  (WIDTH - startX - vertL*3 - horizL) + ',' +  (startY - h*3)  ).attr ("stroke-width", "3"),
                      "rightTopFront",
                      WIDTH - startX - vertL*3 - horizL + (vertL - horizL - critterSize) / 2 - forwardOffset,
                      topCritterY);

var rightMiddleBackSpot = new Spot(gridPaper.path('M ' +  (WIDTH - startX - vertL*2) + ',' +  (startY - h*2) + ' L ' +  (WIDTH - startX - vertL*2 - horizL) + ',' +  (startY - h*2) + ' L ' +  (WIDTH - startX - vertL - horizL) + ',' +  (startY - h) + ' L ' +  (WIDTH - startX - vertL) + ',' +  (startY - h) + ' L ' +  (WIDTH - startX - vertL*2) + ',' +  (startY - h*2) ).attr ("stroke-width", "3"),
                      "rightMiddleBack",
                      WIDTH - startX - vertL*2 + (vertL - horizL - critterSize) / 2 - forwardOffset,
                      midCritterY);

 var rightMiddleFrontSpot = new Spot(gridPaper.path('M ' +  (WIDTH - startX - vertL*2 - horizL) + ',' +  (startY - h*2) + ' L ' +  (WIDTH - startX - vertL*2 - horizL*2) + ',' +  (startY - h*2) + ' L ' +  (WIDTH - startX - vertL - horizL*2) + ',' +  (startY - h) + ' L ' +  (WIDTH - startX - vertL - horizL) + ',' +  (startY - h) + ' L ' +  (WIDTH - startX - vertL*2 - horizL) + ',' +  (startY - h*2) ).attr ("stroke-width", "3"),
                      "rightMiddleFront",
                      WIDTH - startX - vertL*2 - horizL + (vertL - horizL - critterSize) / 2 - forwardOffset,
                      midCritterY);

var rightBottomBackSpot = new Spot(gridPaper.path('M' +  (WIDTH - startX - vertL) + ',' +  (startY - h) + ' L ' +  (WIDTH - startX - vertL - horizL) + ',' +  (startY - h) + ' L ' +  (WIDTH - startX - horizL) + ',' +  (startY) + ' L ' +  (WIDTH - startX) + ',' +  (startY) + ' L ' +  (WIDTH - startX - vertL) + ',' +  (startY - h)).attr ("stroke-width", "3"),
                      "rightBottomBack",
                      WIDTH - startX - vertL + (vertL - horizL - critterSize) / 2 - forwardOffset,
                      botCritterY);


var rightBottomFrontSpot = new Spot(gridPaper.path('M' +  (WIDTH - startX - vertL - horizL) + ',' +  (startY - h) + ' L ' +  (WIDTH - startX - vertL - horizL*2) + ',' +  (startY - h) + ' L ' +  (WIDTH - startX - horizL*2) + ',' +  (startY) + ' L ' +  (WIDTH - startX - horizL) + ',' +  (startY) + ' L ' +  (WIDTH - startX - vertL - horizL) + ',' +  (startY - h)).attr ("stroke-width", "3"),
                      "rightBottomFront",
                      WIDTH - startX - vertL - horizL + (vertL - horizL - critterSize) / 2 - forwardOffset,
                      botCritterY);


var leftBench = new Spot(gridPaper.path('M' + (0) + ',' + (startY - h*3) + 'L' + (startX + vertL*3 - 30) + ',' + (startY - h*3) + 'L' + (startX- 30) + ',' + (startY) + 'L' + (0) + ',' + (startY) + 'L' + (0) + ',' + (startY - h*3)).attr ("stroke-width", "3"),
					"leftBench",
					10,
					benchCritterY);

var rightBench = new Spot(gridPaper.path('M' + (WIDTH - startX - vertL*3 + 30) + ',' + (startY - h*3) + 'L' + (WIDTH) + ',' + (startY - h*3) + 'L' + (WIDTH) + ',' + (startY) + 'L' + (WIDTH - startX + 30) + ',' + (startY) + 'L' + (WIDTH - startX - vertL*3 + 30) + ',' + (startY - h*3)).attr ("stroke-width", "3"),
					"rightBench",
					WIDTH - critterSize - 10,
					benchCritterY);

var spots = [leftTopBackSpot, leftTopFrontSpot, leftMiddleBackSpot, leftMiddleFrontSpot, leftBottomBackSpot, leftBottomFrontSpot, leftBench,
            rightTopBackSpot, rightTopFrontSpot, rightMiddleBackSpot, rightMiddleFrontSpot, rightBottomBackSpot, rightBottomFrontSpot, rightBench];


 spots.forEach(function(s) {

 	s.getPolygon().attr("stroke-width", "0").attr("stroke", "#704000").attr("stroke-opacity", "0.4").attr("stroke-linecap", "round").attr("stroke-linejoin", "round");
 	s.getPolygon().insertBefore(backgroundBottom);
 });

var leftSpots = [leftTopBackSpot, leftTopFrontSpot, leftMiddleBackSpot, leftMiddleFrontSpot, leftBottomBackSpot, leftBottomFrontSpot, leftBench];
var rightSpots = [rightTopBackSpot, rightTopFrontSpot, rightMiddleBackSpot, rightMiddleFrontSpot, rightBottomBackSpot, rightBottomFrontSpot, rightBench];

var critterDistanceCheck = gridPaper.rect(0, 0, 1, 1);

function critterUpdate(str) {
	var tokens = str.split(',');
	var toUpdate = identifyCritter(tokens[1], tokens[2]);
//	toUpdate.setCurrentHealth(tokens[3]);
	toUpdate.setHealth(tokens[3]);
	toUpdate.setEnergy(tokens[4]);
	toUpdate.setBonusDamage(tokens[5]);
	toUpdate.setDefence(tokens[6]);
}

function Critter(name, maxHealth, maxEnergy, side, pos, passiveName, passiveDescription) {
	this.name = name.toLowerCase();
	this.pos = pos;
    this.imageName = "assets/" + this.name + "/" + this.name + side +".png";
    this.passiveName = passiveName;
    this.passiveDescription = passiveDescription;
    this.attackImageName = "assets/" + this.name + "/" + this.name + "attack" + side + ".png";
    this.blockImageName = "assets/" + this.name + "/" + this.name + "block" + side + ".png";
    this.supportImageName = "assets/" + this.name + "/" + this.name + "support" + side + ".png";
    this.portraitImageName = "assets/" + this.name + "/" + this.name + "portrait" + side + ".png";
//    this.attackOptionImageName = "assets/" +this.name + "/" + this.name + "attackoption" + side + ".png";
//    this.supportOptionImageName = "assets/" + this.name + "/" + this.name + "supportoption" + side + ".png";
//    this.blockOptionImageName = "assets/" + this.name + "/" + this.name + "blockoption" + side + ".png";
	  this.attackOptionImageName = this.attackImageName;
	  this.supportOptionImageName =   this.supportImageName;
      this.blockOptionImageName = this.blockImageName;
    this.damageText = gridPaper.set();
	this.getDamageText = function () {return this.damageText}

    this.x = 100;
    if (side == "left") {
    	this.x = 550;
    } else {
    	this.x = 650;
    }
    this.getX = function(){return this.x}
    this.setX = function(newX) {this.x = newX; }
    this.y = 200;
    this.getY = function(){return this.y}
    this.setY = function(newY) {this.y = newY;}
    this.width = critterSize;
    this.height = critterSize;
    this.image = gridPaper.image(this.imageName, this.x, 150, critterSize, critterSize);
	this.portraitX = 400;
	if (side == "left") {
		if (pos == 3 || pos == 7){
			this.portraitX = 53 + 165;
		} else {
			this.portraitX = 53 - (pos % 4) * 9;
		}
	} else {
		if (pos == 3 || pos == 7){
			this.portraitX = WIDTH - 53 - 165;
		} else {
			this.portraitX = WIDTH - 53 + (pos % 4) * 9 ;
		}

	}
	this.getMaxEnergy = function(){return maxEnergy};
    this.portraitY = 35 + (pos % 4) * 40 ;
    if (pos == 3 || pos == 7) {
    	this.portraitY = 35;
    }
    this.getPortraitX = function() {return this.portraitX};
   	this.getPortraitY = function() {return this.portraitY};
   	this.altImage = this.image.clone();
    this.attackImage = gridPaper.image(this.attackImageName, this.x, 150, critterSize, critterSize);
    this.attackOptionImage = gridPaper.image(this.attackOptionImageName, this.x, 150, critterSize, critterSize).attr('opacity',0.6);
    this.blockOptionImage = gridPaper.image(this.blockOptionImageName, this.x, 150, critterSize, critterSize).attr('opacity',0.6);
    this.supportOptionImage = gridPaper.image(this.supportOptionImageName, this.x, 150, critterSize, critterSize).attr('opacity',0.6);
    this.blockImage = gridPaper.image(this.blockImageName, this.x, 150, critterSize, critterSize);
    this.supportImage = gridPaper.image(this.supportImageName, this.x, 150, critterSize, critterSize);
    this.coordinateImages = function() {
   										this.image.attr('x', this.x).attr('y', this.y);
    									this.altImage.attr('x', this.x).attr('y', this.y);
    									this.attackOptionImage.attr('x', this.x).attr('y', this.y);
    									this.supportOptionImage.attr('x', this.x).attr('y', this.y);
    									this.blockOptionImage.attr('x', this.x).attr('y', this.y);
    									this.attackImage.attr('x', this.x).attr('y', this.y);
    									this.blockImage.attr('x', this.x).attr('y', this.y);
    									this.supportImage.attr('x', this.x).attr('y', this.y);
    									};


    this.getImage = function(){return this.image}
    this.getAttackImage = function(){return this.attackImage}
    this.getAttackOptionImage = function(){return this.attackOptionImage}
    this.getSupportOptionImage = function(){return this.supportOptionImage}
    this.getBlockOptionImage = function(){return this.blockOptionImage}
    this.getBlockImage = function(){return this.blockImage}
    this.getSupportImage = function(){return this.supportImage}
    this.attackImage.hide();
    this.attackOptionImage.hide();
    this.supportOptionImage.hide();
    this.blockOptionImage.hide();
    this.blockImage.hide();
    this.supportImage.hide();
	this.portraitImage = gridPaper.image(this.portraitImageName, this.portraitX - 30, this.portraitY - 30, 64, 64);
	this.getPortraitImage = function() {return this.portraitImage};
	this.portraitImage.hide();
	this.getInfoStr = function () {var infoStr = name + "\n" + "Health: " + this.currentHealth + "/" + this.maxHealth + "\n"
				 + "Energy: " + this.currentEnergy + "/" + this.maxEnergy + "\n"
				 + "Bonus Damage: " + this.bonusDamage + "\n"
				 + "Defence: " + this.defence + "\n"
				 + this.passiveName + ": " + this.passiveDescription;
				 return infoStr;}
	this.updateInfoBox = function(){
		infoBox.setInfo(this.getInfoStr());
	};

    this.getAltImage = function() {return this.altImage};
    this.setImage = function(image) {this.image = image}
    this.images = gridPaper.set();
        this.images.push(this.altImage);
    this.images.push(this.attackImage);
    this.images.push(this.attackOptionImage);
    this.images.push(this.supportOptionImage);
    this.images.push(this.blockOptionImage);
    this.images.push(this.blockImage);
    this.images.push(this.supportImage);

	this.altImage.hide();
 	this.image.hide();

    this.getImages = function () {return this.images};
    this.clickBox = gridPaper.rect(this.x, this.y + 40, 0.8*critterSize, 0.8*critterSize).attr('fill', 'white')
    																				 .attr('opacity', '0').hide();
    this.getClickBox = function() {return this.clickBox};

    if (this.y == topCritterY - 40) {
        this.image.insertBefore(topRow);
    	this.images.insertBefore(topRow);

    } else if (this.y == midCritterY - 40) {
        this.image.insertBefore(middleRow);
    	this.images.insertBefore(middleRow);

    } else if (this.y == benchCritterY - 40) {
    	this.image.insertBefore(benchRow);
    	this.images.insertBefore(benchRow);

    } else {
        this.image.insertBefore(botRow);
    	this.images.insertBefore(botRow);

    }
    this.maxHealth = maxHealth;
    this.maxEnergy = maxEnergy;
	this.defence = 0;
	this.setDefence = function(defence) {this.defence = defence};
    this.bonusDamage = 0;
    this.setBonusDamage = function(bonusDamage) {this.bonusDamage = bonusDamage};
    this.currentEnergy = maxEnergy;
    this.setCurrentEnergy = function(currentEnergy) {this.currentEnergy = currentEnergy};
    this.currentHealth = maxHealth;
    this.setCurrentHealth = function(currentHealth) {this.currentHealth = currentHealth};
    this.name = name;
    this.getName = function(){return this.name}
    this.animating = false;
    this.setAnimating = function(animating){this.animating = animating}
    this.isAnimating = function(){return this.animating}
    this.healthText;
    this.energyText;
    this.abilities;
    this.setAbilities = function(abilities) {this.abilities = abilities;}
    this.removeAbilities = function(){abilities.forEach(function(a) {
        a.remove();
    });}
    this.attackIndicated = false;
    this.attackOptionShown = false;
    this.supportOptionShown = false;
    this.blockOptionShown = false;
    this.supportIndicated = false;
    this.blockIndicated = false;

    var attackGlow;
    var blockGlow;
    var supportGlow;
    this.indicateAttack = function() { if (this.attackIndicated == false) {this.attackIndicated = true; this.attackImage.show();}};
    this.unindicateAttack = function() { if(this.attackIndicated == true) {this.attackImage.hide(); this.attackIndicated = false; return true;} return false};

	this.showAttackOption = function() { if (this.attackOptionShown == false) {this.attackOptionShown = true; this.attackOptionImage.show();}};
	this.hideAttackOption = function() { if(this.attackOptionShown == true) {this.attackOptionImage.hide(); this.attackOptionShown = false; return true;} return false};

	this.showSupportOption = function() { if (this.supportOptionShown == false) {this.supportOptionShown = true; this.supportOptionImage.show();}};
	this.hideSupportOption = function() { if(this.supportOptionShown == true) {this.supportOptionImage.hide(); this.supportOptionShown = false; return true;} return false};

	this.showBlockOption = function() { if (this.blockOptionShown == false) {this.blockOptionShown = true; this.blockOptionImage.show();}};
	this.hideBlockOption = function() { if(this.blockOptionShown == true) {this.blockOptionImage.hide(); this.blockOptionShown = false; return true;} return false};

	this.indicateSupport = function() { if (this.supportIndicated == false) {this.supportIndicated = true; this.supportImage.show();}};
	this.unindicateSupport = function() { if(this.supportIndicated == true) {this.supportImage.hide(); this.supportIndicated = false; return true;} return false};
	this.indicateBlock = function() {  if (this.blockIndicated == false) {this.blockIndicated = true; this.blockImage.show();}};
	this.unindicateBlock = function() { if(this.blockIndicated == true) {this.blockImage.hide(); this.blockIndicated = false; return true;} return false};

    this.effecttts = new Array();
    this.getEffecttts = function () {return this.effecttts};

    //portrait
    if (this.portraitX < 700) {
                                                                    // #cc5200, #FFF1E6#E275D3
        this.healthBar = gridPaper.rect(this.portraitX + 33, this.portraitY - 9, 80, 9).attr("fill", "#D3E275").attr("stroke", "#7CFC00");
        this.healthText = gridPaper.text(this.portraitX + 73, this.portraitY - 5, this.maxHealth + "/" + this.maxHealth).attr({"font-size": 10, "font-family": portraitFont});
        this.energyBar = gridPaper.rect(this.portraitX + 33, this.portraitY + 4, 80, 9).attr("fill", "#759DE2").attr("stroke", "#0000FF");
        this.energyText = gridPaper.text(this.portraitX + 73, this.portraitY + 9, this.maxEnergy + "/" + this.maxEnergy).attr({"font-size": 10, "font-family": portraitFont});
    } else if (this.portraitX > 700) {
        this.healthBar = gridPaper.rect(this.portraitX - 113, this.portraitY - 9, 80, 9).attr("fill", "#D3E275").attr("stroke", "#7CFC00");
        this.healthText = gridPaper.text(this.portraitX - 73, this.portraitY - 5, this.maxHealth + "/" + this.maxHealth).attr({"font-size": 10, "font-family": portraitFont});
        this.energyBar = gridPaper.rect(this.portraitX - 113, this.portraitY + 4, 80, 9).attr("fill", "#759DE2").attr("stroke", "#0000FF");
        this.energyText = gridPaper.text(this.portraitX - 73, this.portraitY + 9, this.maxEnergy + "/" + this.maxEnergy).attr({"font-size": 10, "font-family": portraitFont});
    }
    this.energyText.node.setAttribute("class","donthighlight");
    this.healthText.node.setAttribute("class","donthighlight");


    this.health = 0;
    this.energy = maxEnergy;

    this.setHealth = function(health) {
    if (this.currentHealth - health != 0) {
    	var dHealth =  health - this.currentHealth;
        var dHS = dHealth;
        var color = '#E63B00';
        if (dHealth > 0 ) {
        	dHS = "+" + dHealth;
        	color = '#00A053';
        }
    	var newText = gridPaper.text(this.x + 100, this.y + 100 + 15 * this.damageText.length, dHS).attr({'font-size': 25, 'fill': color, 'font-family': dTextFont});
    	var dt = this.damageText;
    	newText.animate({y : this.y + 100 - 40 + 15 * this.damageText.length, 'opacity': 0}, 2500, function(){newText.remove(); dt.splice(0,1)});

    	this.damageText.push(newText);
    }
    if (health < 0) {
    	this.currentHealth = 0;
    } else {
    	this.currentHealth = health;
    }

        if (this.portraitX < 700) {
            this.healthBar.animate({width: health/maxHealth * 80}, 8);
	       } else {
            this.healthBar.animate({width: health/maxHealth * 80, x: this.portraitX - 31 - health/maxHealth * 80}, 8);
	       }
        this.healthText.attr('text', health + "/" + maxHealth);
        this.healthText.node.setAttribute("class","donthighlight");
    };

    this.getCurrentHealth = function() {
    	return this.currentHealth;
    }

	this.dEnergyText = gridPaper.set();

    this.setEnergy = function(energy) {
        if (this.currentEnergy - energy != 0) {
        	var dEnergy =  energy - this.currentEnergy
        	var dES = dEnergy;
        	if (dEnergy > 0 ) {
        		dES = "+" + dEnergy;
        	}
    		var newText = gridPaper.text(this.x + 150, this.y + 100 + 15 * this.dEnergyText.length,dES).attr({'font-size': 25, 'fill': '#065C93', 'font-family': dTextFont});
    		var det = this.dEnergyText;
    		newText.animate({y : this.y + 100 - 40 + 15 * this.dEnergyText.length, 'opacity': 0}, 2500, function(){newText.remove(); det.splice(0,1)});
    		this.dEnergyText.push(newText);
    	}

    	this.currentEnergy = energy;
        if (this.portraitX < 700) {
            this.energyBar.animate({width: energy/maxEnergy * 80}, 8);
        } else {
            this.energyBar.animate({width: energy/maxEnergy * 80, x: this.portraitX -31 - energy/maxEnergy * 80}, 8);
        }
        this.energyText.attr('text', energy + "/" + maxEnergy);
        this.energyText.node.setAttribute("class","donthighlight");
    };

    this.getCurrentEnergy = function() {
    	return this.currentEnergy;
    }

	this.portraitClickBox = gridPaper.circle(this.portraitX, this.portraitY, 32).attr('fill-opacity','0').attr("fill", "BEBEBE").attr('stroke-opacity', '0').hide();
	this.getPortraitClickBox = function() {return this.portraitClickBox};
	this.screen = gridPaper.circle(this.portraitX, this.portraitY, 32).attr('stroke-width', '3').attr('fill','#BEBEBE').attr('fill-opacity','0.7').attr('stroke-opacity', '0').hide();
	this.portrait = new gridPaper.set();
	this.faceAndCircle = new gridPaper.set();
	this.portrait.push( this.portraitImage, this.energyText, this.energyBar, this.healthText, this.healthBar);
    this.faceAndCircle.push( this.portraitImage);
    this.portrait.hide();
    this.portraitClickBox.click(function() {
    	if (!calculating) {
			ws.send("click,critter," + name + "," + side + "," + id);
		}
	});
	this.portraitClickBox.hover(
		function() {
			document.body.style.cursor = 'pointer';
			infoBox.setInfo(name + "\n" + "Health: " + maxHealth + "/" + maxHealth + "\n"
				 + "Energy: " + maxEnergy + "/" + maxEnergy + "\n"
				 + "Bonus Damage: " + 0 + "\n"
				 + "Defence: " + 0 + "\n"
				 + passiveName + ": " + passiveDescription);
			infoBox.show();

		},
		function () {
			document.body.style.cursor = 'auto';
			infoBox.hide(false);
		}
	);
	this.clickBox.hover(
		function() {
			document.body.style.cursor = 'pointer';
			infoBox.setInfo(name + "\n" + "Health: " + maxHealth + "/" + maxHealth + "\n"
				 + "Energy: " + maxEnergy + "/" + maxEnergy + "\n"
				 + "Bonus Damage: " + 0 + "\n"
				 + "Defence: " + 0 + "\n"
				 + passiveName + ": " + passiveDescription);
			infoBox.show();

		},
		function () {
			document.body.style.cursor = 'auto';
			infoBox.hide(false);
		}
	);
	this.getScreen = function() {return this.screen};
	this.getPortraitCircle = function() {return this.portraitCircle};
    this.getPortrait = function() {return this.portrait};
    this.token = new CritterToken(this.portraitImageName, pos, side);

    this.getToken = function() {return this.token};

        this.healthText.hide();
    this.energyText.hide();
}

function CritterToken(imageName, pos, side) {
	pos = pos % 4;
	this.x = 250 + 65 *(pos);
	if (side == "right") {
		this.x = WIDTH - 250 - 65 *(pos);
	}
	this.y = 75;
	this.image = gridPaper.image(imageName, this.x - 25 , this.y - 25, 50, 50);

	this.circle = gridPaper.circle(this.x, this.y, 25).attr("fill", "#fff").attr('fill-opacity', '0').attr('stroke-width', '2').attr('stroke', '#704000');
	this.portrait = gridPaper.set(this.image, this.circle);
	this.portrait.hover(
		function() {
			document.body.style.cursor = 'pointer';
		},
		function () {
			document.body.style.cursor = 'auto';
		}
	);
	this.getPortrait = function () {return this.portrait};
	this.spot = null;
	this.getSpot = function () {return this.spot};
	this.setSpot = function (spot) {this.spot = spot};
	this.getCircle = function(){return this.circle};

}

var topRow = gridPaper.rect(0, 0, 0, 0);
var middleRow = gridPaper.rect(0, 0, 0, 0);
var benchRow = gridPaper.rect(0, 0, 0, 0);
var botRow = gridPaper.rect(0, 0, 0, 0);


var optionPolySet = gridPaper.set();

function showPossibleMoves(message, justPressed) {
    var messageTokens = message.split(",");
    while(optionPolySet.length > 0) {
    	optionPolySet.pop().remove();
    }
    if (justPressed) {
	abilities.forEach(function(a){
		a.getRect().attr("fill-opacity", 0);
		a.getRect().attr("fill", "white");
		a.setClicked(false);
	});
	}
 	var abilityName = messageTokens[2];
    var hoverColor;
    var spotInfoTokens = [];
    abilities.forEach(function(a){
		if (messageTokens[2] == a.getName()) {
			a.setClicked(true);
			a.getRect().attr("fill", "#999999");
			a.getRect().attr("fill-opacity", 0.5);
    	}
	});

    for (var j = 3; j < messageTokens.length - 1; j++) {
    	spotInfoTokens[j - 3] = messageTokens[j];
    }
    spotInfoTokens.forEach(function(si) {
        var coords = si.split(".")[1];
        var optionPoly = createPathFromCoords(coords);
   //     optionPoly.insertBefore(backgroundBottom);
        var spotNames = si.split(".")[0].split("-");
        var spotsToIndicate = [];
        var side = si.split(".")[2];
        for (var i = 0; i < spotNames.length; i++) {
        	spotsToIndicate.push(identifySpot(spotNames[i]));
        }
        optionPolySet.push(optionPoly);
   		if (messageTokens[1] == "move") {
   			hoverColor = moveColor;
        } else if (messageTokens[1] == "support") {
 			hoverColor = supportColor;
        } else if (messageTokens[1] == "attack") {
 			hoverColor = attackColor;
        }
        optionPoly.click(function() {
   			spotsToIndicate.forEach(function(s) {
   				ws.send("click,spot," + spotNames[0] + "," + side + "," + id);
   					while(optionPolySet.length > 0) {
    					optionPolySet.pop().remove();
    				}
   				});
   			});
        optionPoly.hover(
   			function() {
   				optionPoly.attr("fill", hoverColor).attr("fill-opacity", 0.5);
   				document.body.style.cursor = 'pointer';
   			},
   			function() {
   				optionPoly.attr("fill-opacity", 0);
   				document.body.style.cursor = 'auto';
   			});

//        }

    });


}
		var topyBoost = 25;
		var midyBoost = 25;
		var benchyBoost = 25;
		var botyBoost = 25;


function critterLayering(critter) {
          var newSize = 's1';
          var botset = gridPaper.set();
          var topset = gridPaper.set();
          var midset = gridPaper.set();
          var benchset = gridPaper.set();
          if (critter.getY() == botCritterY) {
          	 critter.getImage().insertBefore(botRow);
          	 critter.getImages().insertBefore(botRow);
          } else if (critter.getY() == midCritterY) {
          	critter.getImage().insertBefore(middleRow);
          	critter.getImages().insertBefore(middleRow);
          } else if (critter.getY() == topCritterY) {
            critter.getImage().insertBefore(topRow);
          	critter.getImages().insertBefore(topRow);
          }

          critters.forEach(function(f) {
          	if (f.getY() == botCritterY) {
          	 	botset.push(f.getClickBox());
          	} else if (f.getY() == benchCritterY) {
          		benchset.push(f.getClickBox());
          	} else if (f.getY() == midCritterY) {
          	 	midset.push(f.getClickBox());
          	} else if (f.getY() == topCritterY) {
          	 	topset.push(f.getClickBox());
          	}
          });

          topset.toFront();
          midset.toFront();
          botset.toFront();

}

function move(moves) {
//		alert(moves);

        var images = gridPaper.set();
        var nextStr = "";
        var smallTokens = moves.split(",");
        var critter = identifyCritter(smallTokens[1], smallTokens[2]);
        images.push(critter.getAltImage());
        var goingUp = false;
        var spot = identifySpot(smallTokens[3]);

        if (critter.getY() > spot.getCritterY()) {

        	goingUp = true;
        	critter.setX(spot.getCritterX());
          	critter.setY(spot.getCritterY());
        	critterLayering(critter);
        } else {
        	 if (critter.getY() == midCritterY) {
          		critter.getImage().insertBefore(middleRow);
          		critter.getImages().insertBefore(middleRow);
          	} else if (critter.getY() == topCritterY) {
	            critter.getImage().insertBefore(topRow);
	          	critter.getImages().insertBefore(topRow);
          	}
        	critter.setX(spot.getCritterX());
         	critter.setY(spot.getCritterY());
        }


          var newSize = 's1';

          if (critter.getY() == botCritterY) {
          	 newSize = 's1';
          	 critter.getClickBox().attr({x: critter.getX() + 20, y: critter.getY() + botyBoost});
          } else if (critter.getY() == benchCritterY) {
          	newSize = 's0.97';
            critter.getClickBox().attr({x: critter.getX() + 20, y: critter.getY() + benchyBoost});
          }	else if (critter.getY() == midCritterY) {
          	newSize = 's0.95';
	  		critter.getClickBox().attr({x: critter.getX() + 20, y: critter.getY() + midyBoost});
          } else if (critter.getY() == topCritterY) {
            newSize = 's0.90';
            critter.getClickBox().attr({x: critter.getX() + 20, y: critter.getY() + topyBoost});
          }

         setTimeout(function() {
            critter.getImage().animate({x: spot.getCritterX(), y: spot.getCritterY(), transform: newSize}, smallTokens[4] - 300,
            function () {
            	if (!goingUp) {
            		critterLayering(critter);
            	}
            	critter.getImages().forEach(function(fi){
					fi.animate({transform: newSize});
		  		});
		  		critter.getClickBox().animate({transform: newSize});
            });
            critter.getAltImage().animate({x: spot.getCritterX(), y: spot.getCritterY(), transform: newSize}, smallTokens[4] - 300);
            critter.getImages().animate({x: spot.getCritterX(), y: spot.getCritterY(), transform: newSize}, smallTokens[4] - 300);

		},250);




}

leftSpots.forEach(function(spot) {
    spot.getPolygon().click(function() {
        ws.send("click,spot," + spot.getName() + ",left" + "," + id);
    });
});
rightSpots.forEach(function(spot) {
    spot.getPolygon().node.onclick = function() {
        ws.send("click,spot," + spot.getName() + ",right" + "," + id);
    };
});

function identifySpot(str) {
    for (var i = 0; i < spots.length; i++) {
        if (spots[i].getName() == str) {
            return spots[i];
        }
    }
}

function identifyCritter(name, side) {
if (side == "left") {
    for (var i = 0; i < leftCritters.length; i++) {
        if (leftCritters[i].getName().toLowerCase() == name.toLowerCase()) {
            return leftCritters[i];
        }
    }
}  else if (side == "right") {
    for (var i = 0; i < rightCritters.length; i++) {
        if (rightCritters[i].getName().toLowerCase() == name.toLowerCase()) {
            return rightCritters[i];
        }
    }
}
}
