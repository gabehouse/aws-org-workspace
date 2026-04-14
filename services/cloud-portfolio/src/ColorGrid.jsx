import React, { useState, useRef, useEffect } from 'react';

import { generateClient } from 'aws-amplify/data';

//import styles from './ColorGrid.css';
const client = generateClient();






const ColorGrid = () => {

  const [grid, setGrid] = useState([]);
  const cellSize = useRef(20);
  const gridWidth = 100, gridHeight = 100;
  const [viewWidth, setGridWidth] = useState(10);
  const [viewHeight, setGridHeight] = useState(10);
  const [viewportDimensions, setViewportDimensions] = useState({ w: 0, h: 0 });
  const G = useRef(new Array(gridHeight).fill('white').map(() => new Array(gridWidth).fill('white')));
  const GInfo = useRef(new Array(gridHeight).fill('n/a;n/a').map(() => new Array(gridWidth).fill('n/a;n/a')));
  const [viewport, setViewport] = useState(new Array(1).fill('white').map(() => new Array(1).fill('white')));
  const [loading, setLoading] = useState(true);
  const [resizing, setResizing] = useState(false);

  const viewportCalls = useRef(0);
  const [infoBox, setInfoBox] = useState({ x: -1, y: -1, loc: "", date: "" });
  const squarePlaced = useRef(false);



  function logTitleX(grid) {
    for (let i = 0; i < grid.length; i++) {
      for (let j = 0; j < grid[i].length; j++) {
        if (grid[i][j] == 'black') {
          return "logtitlex " + j;

        }
      }
    }
  }

  function fillViewport(h, w) {
    console.log('fillviewport');
    /*   viewport.current = new Array(screenHeightInCells).fill('0').map(()=> new Array(screenWidthInCells).fill('0'))
       for (let i = 0; i < screenHeightInCells; i++) {
         for (let j = 0; j < screenWidthInCells; j++) {
           viewport.current[i][j] = G.current[i+viewportStartY][j+viewportStartX];
         }
       } */
    //  const h = viewportDimensions.h, w = viewportDimensions.w;
    const y = Math.ceil(gridHeight / 2 - h / 2);
    const x = Math.ceil(gridWidth / 2 - w / 2);
    var viewportTemp = new Array(h).fill('0').map(() => new Array(w).fill('0'))
    console.log(", x = " + x);
    for (let i = 0; i < h; i++) {
      for (let j = 0; j < w; j++) {
        viewportTemp[i][j] = G.current[i + y][j + x];
      }
    }

    setViewport(viewportTemp);
    viewportCalls.current += viewportCalls.current;
    console.log(logTitleX(viewport));
    //  console.log("viewport " + viewport.current);
    setResizing(false);
  }


  const title = "GABE HOUSE";
  const minTitleWidth = title.length * 4 - 1; // Each letter is 3 wide + 1 space, minus 1 at the end

  const loadGrid = async () => {
    const removeOld = false;
    var noneFound = false;

    //  Fetch all existing grid items once
    const { data: items, errors } = await client.models.Grid.list();
    console.log("start loadgrid " + items);
    if (items.length == 0) {
      console.log('nonefound = true');
      noneFound = true;
    }

    if (removeOld) {
      console.log(`Found ${items.length} items to delete`);

      // Step 2: Delete all items in parallel for efficiency
      if (items.length > 0) {
        const deletePromises = items.map(item =>
          client.models.Grid.delete({ id: item.id })
        );

        await Promise.all(deletePromises);
        console.log(`Successfully deleted ${items.length} items`);
      } else {
        console.log("No items to delete");
      }
    }
    if (noneFound || removeOld) {
      console.log('nonefound');
      // initialize empty
      var freshGrid = new Array(gridHeight).fill('white').map(() => new Array(gridWidth).fill('white'));

      addTitle(freshGrid, gridWidth, gridHeight, minTitleWidth, title);

      await client.models.Grid.create({
        id: '0',
        content: freshGrid.toString(),
        info: GInfo.current.toString(),
      });
    }






    // Fetch the updated grid items and log the content
    const { data: newItems, errors: updatedErrors } = await client.models.Grid.list();
    console.log("data " + newItems);
    // console.log(newItems[0].content);
    var dbGrid = newItems[0].content.split(',');
    var dbInfoGrid = newItems[0].info.split(',');

    for (var i = 0; i < dbInfoGrid.length; i++) {
      if (dbInfoGrid[i] != 'n/a;n/a') {
        console.log(dbInfoGrid[i]);
      }
    }
    for (let i = 0; i < dbGrid.length; i++) {
      //  if (G.current[Math.floor(i/gridHeight)][i%gridWidth] == 'white') {
      G.current[Math.floor(i / gridHeight)][i % gridWidth] = dbGrid[i];
      GInfo.current[Math.floor(i / gridHeight)][i % gridWidth] = dbInfoGrid[i];
      //   }
    }
    printColors(G.current);

    handleResize();
    console.log('done loading');

    setLoading(false);


  }


  useEffect(() => {

    console.log('useeffect');
    loadGrid();

    //   fillViewport();


    window.addEventListener('resize', handleResize);
    return () => { window.removeEventListener('resize', handleResize) }

  }, [])


  // useEffect(()=> {
  // fillViewport();
  //  }, [viewportDimensions]);

  function handleResize() {
    console.log("handle resize");
    setResizing(true);

    const windowWidth = document.documentElement.clientWidth; // works well
    const windowHeight = document.documentElement.clientHeight;

    const requiredWidth = minTitleWidth * 1.25;

    cellSize.current = Math.min(20, Math.floor(windowWidth / requiredWidth));
    cellSize.current = Math.max(5, cellSize.current);

    console.log("window width = " + windowWidth + "cellsize " + cellSize.current + "requiredWidth " + requiredWidth);

    let screenWidthInCells = Math.ceil(windowWidth / cellSize.current);
    let screenHeightInCells = Math.ceil(windowHeight / cellSize.current);
    console.log("swic,shic = " + screenWidthInCells + ", " + screenHeightInCells);
    fillViewport(screenHeightInCells, screenWidthInCells);
    setViewportDimensions({ w: screenWidthInCells, h: screenHeightInCells });

  }




  const printGridStr = async () => {
    console.log("pgs");
    const { data: items, errors: errors } = await client.models.Grid.list();
    console.log(items);
    items[0].content.split(',').map((c) => { if (c != 'white') console.log(c); })
    // items[0].info.split(',').map((c)=>{if (c != 'white') console.log(c);})


  }
  function showInfo(i, j) {
    const viewportStartY = Math.ceil(gridHeight / 2 - viewportDimensions.h / 2);
    const viewportStartX = Math.ceil(gridWidth / 2 - viewportDimensions.w / 2);
    const infoTokens = GInfo.current[i + viewportStartY][j + viewportStartX].split(';');
    const loc = infoTokens[0];
    const date = infoTokens[1];
    setInfoBox({ y: i * cellSize.current, x: j * cellSize.current, loc: loc, date: date });
  }
  function hideInfo() {
    setInfoBox({ x: -1, y: -1, loc: "", date: "" });
    console.log("hideinfo ");
  }
  const updateGrid = async (i, j, color) => {
    console.log("updategrid");
    squarePlaced.current = true;
    const viewportStartY = Math.ceil(gridHeight / 2 - viewportDimensions.h / 2);
    const viewportStartX = Math.ceil(gridWidth / 2 - viewportDimensions.w / 2);
    G.current[i + viewportStartY][j + viewportStartX] = color;


    viewport[i][j] = color;
    // get location and date2
    var text;
    const today = new Date();
    try {
      const response = await fetch('https://ip2c.org/s');
      text = await response.text();
      GInfo.current[i + viewportStartY][j + viewportStartX] = text.split(';')[2] + ';' + today.toLocaleDateString();
    } catch {

      GInfo.current[i + viewportStartY][j + viewportStartX] = 'Inaccessible;' + today.toLocaleDateString();
    }


    // add to local info grid

    // const data = await response.json();
    // update database with local info grid
    printColors(G.current);
    await client.models.Grid.update({
      id: 0,
      content: G.current.toString(),
      info: GInfo.current.toString(),
    });
  }

  function printColors(grid) {
    console.log('printcolors ');
    for (var i = 0; i < grid.length; i++) {
      for (var j = 0; j < grid.length; j++) {
        if (grid[i][j] != 'white') {
          console.log(grid[i][j]);
        }
      }
    }
  }


  function addTitle(gCurrent, gridWidth, gridHeight, minTitleWidth, title) { // fills gCurrent with the title at its center
    const getTitlePattern = () => {
      return {
        'G': [
          [1, 1, 1],
          [1, 0, 0],
          [1, 0, 1],
          [1, 1, 1]
        ],
        'A': [
          [1, 1, 1],
          [1, 0, 1],
          [1, 1, 1],
          [1, 0, 1]
        ],
        'B': [
          [1, 1, 0],
          [1, 0, 1],
          [1, 1, 0],
          [1, 1, 1]
        ],
        'E': [
          [1, 1, 1],
          [1, 0, 0],
          [1, 1, 0],
          [1, 1, 1]
        ],
        'H': [
          [1, 0, 1],
          [1, 0, 1],
          [1, 1, 1],
          [1, 0, 1]
        ],
        'O': [
          [1, 1, 1],
          [1, 0, 1],
          [1, 0, 1],
          [1, 1, 1]
        ],
        'U': [
          [1, 0, 1],
          [1, 0, 1],
          [1, 0, 1],
          [1, 1, 1]
        ],
        'S': [
          [1, 1, 1],
          [1, 0, 0],
          [0, 1, 1],
          [1, 1, 1]
        ],
        ' ': [
          [0, 0, 0],
          [0, 0, 0],
          [0, 0, 0],
          [0, 0, 0]
        ]
      };
    };
    const startX = Math.floor(gridWidth / 2 - minTitleWidth / 2);
    const startY = gridHeight / 2 - 2;
    const titlePattern = getTitlePattern();

    for (var i = 0; i < title.length; i++) {
      for (var j = 0; j < 4; j++) {
        for (var k = 0; k < 3; k++) {
          if (titlePattern[title[i]][j][k]) {
            gCurrent[startY + j][startX + i * 4 + k] = 'black';
            //      console.log(startY + j + ', ' + (startX +i * 4 + k) + ',' + gCurrent[startY + j][startX + i * 4 + k] );
          }
        }
      }
    }
  }
  function isSquarePlaced() {
    return squarePlaced.current;
  }


  if (loading) {
    return (
      <div></div>
    );
  }
  //  console.log(logTitleX(viewport));
  console.log('render ' + logTitleX(viewport) + " cellsize " + cellSize.current);
  return (
    <div>
      {viewport.map((row, i) => (
        row.map((cell, j) => (
          <Square key={`${i}-${j}-${viewportDimensions.w}-${viewportDimensions.h}-${cellSize.current}`} i={i} j={j} cellWidth={cellSize.current} cellHeight={cellSize.current} updateGrid={updateGrid} initColor={viewport[i][j]} showInfo={() => showInfo(i, j)} hideInfo={hideInfo} isSquarePlaced={isSquarePlaced} />
        ))
      ))}
      <InfoBox key={`${infoBox.x}-${infoBox.y}`} x={infoBox.x} y={infoBox.y} loc={infoBox.loc} date={infoBox.date} />
    </div>
  );
};

const tooltipStyle = {
  position: 'relative',
  display: 'inline-block',
  backgroundColor: '#333',
  color: 'white',
  padding: '8px 12px',
  borderRadius: '4px',

  width: '90px',
  height: '45px',
  lineHeight: '7px',
  fontSize: '12px',
  paddingTop: "0px",
};

const tooltipArrowStyle = {
  content: '""',
  position: 'absolute',
  top: '100%',
  left: '50%',
  marginLeft: '-8px',
  borderWidth: '8px',
  borderStyle: 'solid',
  borderColor: '#333 transparent transparent transparent',
};

function InfoBox({ x, y, loc, date }) {
  console.log('infobox ' + loc + ' ' + date);
  if (x < 0) return <div></div>
  return (
    <div styles={tooltipStyle} style={{
      position: 'relative',
      display: 'inline-block',
      backgroundColor: '#333',
      color: 'white',
      padding: '8px 12px',
      borderRadius: '4px',
      left: `${x - 5}px`,
      top: `${y + 35}px`,
      width: '100px',
      height: '45px',
      lineHeight: '7px',
      fontSize: '12px',
      paddingTop: "0px",
    }}>
      <p><b>Loc: </b>{loc}</p>
      <p><b>Date: </b>{date}</p>
      <div style={{
        content: '""',
        position: 'absolute',
        bottom: '100%',
        left: '14%',
        marginLeft: '-8px',
        borderWidth: '8px',
        borderStyle: 'solid',
        borderColor: 'transparent transparent #333 transparent',
      }}></div>
    </div>

  )

}

function Square({ i, j, cellWidth, cellHeight, updateGrid, initColor, showInfo, hideInfo, isSquarePlaced }) {
  const [color, setColor] = useState(initColor);
  const getRandomColor = () => {
    const letters = '0123456789ABCDEF';
    let color = '#';
    for (let i = 0; i < 6; i++) {
      color += letters[Math.floor(Math.random() * 16)];
    }
    return color;
  };

  function onSquareClick(squarePlaced) {
    if (isSquarePlaced()) return;
    console.log(color);
    if (color == 'white') {

      var newColor = getRandomColor();
      updateGrid(i, j, newColor);
      setColor(newColor);
    }

  }

  function onMouseOver() {
    if (color != 'white' && color != 'black')
      showInfo(i, j);
  }
  function onMouseLeave() {
    if (color != 'white' && color != 'black')
      hideInfo();
  }
  //  console.log(color);
  return (
    <div className="square" onClick={onSquareClick} onMouseOver={onMouseOver} onMouseLeave={onMouseLeave}
      style={{
        width: `${cellWidth}px`,
        height: `${cellHeight}px`,
        borderWidth: '0px',
        borderColor: 'black',
        position: 'absolute',
        backgroundColor: color,
        left: `${j * cellWidth}px`,
        top: `${i * cellHeight}px`,
      }}>

    </div>
  );

}
export default ColorGrid;
