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
from mycreatepathnetwork import *
from mynavigatorhelpers import *
from Queue import PriorityQueue
from collections import defaultdict


###############################
### AStarNavigator
###
### Creates a path node network and implements the FloydWarshall all-pairs shortest-path algorithm to create a path to the given destination.
			
class AStarNavigator(NavMeshNavigator):

	def __init__(self):
		NavMeshNavigator.__init__(self)
		

	### Create the pathnode network and pre-compute all shortest paths along the network.
	### self: the navigator object
	### world: the world object
	def createPathNetwork(self, world):
		self.pathnodes, self.pathnetwork, self.navmesh = myCreatePathNetwork(world, self.agent)
		return None
		
	### Finds the shortest path from the source to the destination using A*.
	### self: the navigator object
	### source: the place the agent is starting from (i.e., it's current location)
	### dest: the place the agent is told to go to
	def computePath(self, source, dest):
		### Make sure the next and dist matricies exist
		if self.agent != None and self.world != None: 
			self.source = source
			self.destination = dest
			### Step 1: If the agent has a clear path from the source to dest, then go straight there.
			###   Determine if there are no obstacles between source and destination (hint: cast rays against world.getLines(), check for clearance).
			###   Tell the agent to move to dest
			### Step 2: If there is an obstacle, create the path that will move around the obstacles.
			###   Find the pathnodes closest to source and destination.
			###   Create the path by traversing the self.next matrix until the pathnode closes to the destination is reached
			###   Store the path by calling self.setPath()
			###   Tell the agent to move to the first node in the path (and pop the first node off the path)
			if clearShot(source, dest, self.world.getLines(), self.world.getPoints(), self.agent):
				self.agent.moveToTarget(dest)
			else:
				start = findClosestUnobstructed(source, self.pathnodes, self.world.getLinesWithoutBorders())
				end = findClosestUnobstructed(dest, self.pathnodes, self.world.getLinesWithoutBorders())
				if start != None and end != None:
					print len(self.pathnetwork)
					newnetwork = unobstructedNetwork(self.pathnetwork, self.world.getGates())
					print len(newnetwork)
					closedlist = []
					path, closedlist = astar(start, end, newnetwork)
					if path is not None and len(path) > 0:
						path = shortcutPath(source, dest, path, self.world, self.agent)
						self.setPath(path)
						if self.path is not None and len(self.path) > 0:
							first = self.path.pop(0)
							self.agent.moveToTarget(first)
				# 	else:
				# 		# print "tried to stop"
				# 		self.agent.stopMoving()
				# else:
				# 	# print "tried to stop"
				# 	self.agent.stopMoving()
						# self.agent.moveTarget = self.agent.position
		return None
		
	### Called when the agent gets to a node in the path.
	### self: the navigator object
	def checkpoint(self):
		myCheckpoint(self)
		return None

	### This function gets called by the agent to figure out if some shortcutes can be taken when traversing the path.
	### This function should update the path and return True if the path was updated.
	def smooth(self):
		return mySmooth(self)

	def update(self, delta):
		myUpdate(self, delta)


def unobstructedNetwork(network, worldLines):
	newnetwork = []
	for l in network:
		hit = rayTraceWorld(l[0], l[1], worldLines)
		if hit == None:
			newnetwork.append(l)
	return newnetwork



def straight_line_dist(p1, p2):
	return math.sqrt(float((p1[0] - p2[0])**2 + (p1[1] - p2[1])**2))

def astar(init, goal, network):
	path = []
	open = []
	closed = []
	### YOUR CODE GOES BELOW HERE ###
	visited = set()
	connected = defaultdict(set)
	for edge in network:
		connected[edge[0]].add(edge[1])
		connected[edge[1]].add(edge[0])
	fringe = PriorityQueue()
	h = straight_line_dist(init, goal)
	# item in fringe in format (g(cost + heuristic), cost, heuristic, pt, path(init, nodea, nodeb ...))
	fringe.put((h, h, 0, init, ()))
	found_solution = False
	n = None
	while not fringe.empty():
		n = fringe.get()
		g, h, c, pt, path = n
		if pt in visited:
			continue
		if pt == goal:
			found_solution = True
			break
		visited.add(pt)
		for child in connected[pt] - visited:
			new_h = straight_line_dist(child, goal)
			new_c = c + straight_line_dist(pt, child)
			new_g = new_c + new_h
			new_path = list(path)
			new_path.append(pt)
			successor = (new_g, new_h, new_c, child, tuple(new_path))
			fringe.put(successor)
	if not found_solution:
		path = []
	else:
		path = list(n[4])
		path.append(goal)

	closed = list(visited)
	### YOUR CODE GOES ABOVE HERE ###
	return path, closed
	
	


def myUpdate(nav, delta):
	### YOUR CODE GOES BELOW HERE ###
	if nav.agent.moveTarget is not None and nav.destination is not None:
		if not clearShot(nav.agent.position, nav.agent.moveTarget, nav.agent.world.getLinesWithoutBorders(), nav.agent.world.getPoints(), nav.agent):
			print "replanning"
			nav.agent.stopMoving()
			nav.agent.navigateTo(nav.destination) # computePath(nav.agent.position, nav.destination) #nav.agent.targets[0]
	### YOUR CODE GOES ABOVE HERE ###
	return None



def myCheckpoint(nav):
	### YOUR CODE GOES BELOW HERE ###
	resources = nav.agent.world.resources
	if resources is not None and len(resources) > 0:
		nav.agent.setTargets(map(lambda x:x.getLocation(), list(resources)))
	else:
		nav.agent.targets = []
		nav.agent.stopMoving()
	print nav.agent.targets
	### YOUR CODE GOES ABOVE HERE ###
	return None


### Returns true if the agent can get from p1 to p2 directly without running into an obstacle.
### p1: the current location of the agent
### p2: the destination of the agent
### worldLines: all the lines in the world
### agent: the Agent object
def clearShot(p1, p2, worldLines, worldPoints, agent):
	### YOUR CODE GOES BELOW HERE ###
	valid = False
	source, target = p1, p2
	line = (p1,p2)
	if source is None or target is None:
		return False
	if rayTraceWorld(source, target, worldLines) == None:
		valid = True
		for pt in worldPoints:
			if (minimumDistance(line, pt) <= agent.getMaxRadius()):
				valid = False
				break
	
	### YOUR CODE GOES ABOVE HERE ###
	return valid

