'''
 * Copyright (c) 2014, 2015 Entertainment Intelligence Lab, Georgia Institute of Technology.
 * Originally developed by Mark Riedl.
 * Last edited by Mark Riedl 05/2015
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
'''

import sys, pygame, math, numpy, random, time, copy
from pygame.locals import *

from constants import *
from utils import *
from core import *

eps = 0.001

def scanElim(cellsize, cells, obstacles):
    # Shamelessly coppied and edited from core.py lines 1006-1018
    for column in range(0, len(cells)):
        for row in range(0, len(cells[0])):
            bottomright = (column * cellsize, (row + 1) * cellsize)
            bottomleft = (column * cellsize, row * cellsize)
            topright = ((column + 1) * cellsize, (row + 1) * cellsize)
            topleft = ((column + 1) * cellsize, row * cellsize)
            # Check if the cell is inside an obstacle (does not collide, but we still don't want to include it)
            if insideObstacle(bottomleft, obstacles):
                cells[column][row] = False
            else:
                for o in obstacles:
                    c = False
                    # Check if a cell collides with a obstacle
                    for l in o.getLines():
                        for r in ((topleft, topright), (topright, bottomright), (bottomright, bottomleft), (bottomleft, topleft)):
                            hit = calculateIntersectPoint(l[0], l[1], r[0], r[1])
                            if hit is not None:
                                cells[column][row] = False
    return cells

# Creates a grid as a 2D array of True/False values (True =  traversable). Also returns the dimensions of the grid as a (columns, rows) list.
def myCreateGrid(world, cellsize):
    grid = None
    dimensions = (0, 0)
    ### YOUR CODE GOES BELOW HERE ###

    wx, wy = world.getDimensions()
    wx = int(wx / cellsize)
    wy = int(wy / cellsize)

    dimensions = (wx, wy)

    grid = []
    for i in range(dimensions[0]):
        grid.append([True]*dimensions[1])

    ## print("created grid that has ", dimensions[0], " columns and ", dimensions[1], " rows")
    obstacles = world.getObstacles()
    grid = scanElim(cellsize, grid, obstacles)


    ### YOUR CODE GOES ABOVE HERE ###
    return grid, dimensions

