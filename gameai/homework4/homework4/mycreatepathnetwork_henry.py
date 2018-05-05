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

import sys, pygame, math, numpy, random, time, copy, operator
from pygame.locals import *

from constants import *
from utils import *
from core import *
from collections import defaultdict
import itertools

# Creates a pathnode network that connects the midpoints of each navmesh together
def myCreatePathNetwork(world, agent = None):
	nodes = []
	edges = []
	polys = []
	### YOUR CODE GOES BELOW HERE ###
	if agent is None:
		agent = world.getAgent()
	polys = build_convex_hulls(world, agent) 
	nodes, edges = build_nodes(world, polys)
	edges = prune_edges(edges, world, agent)
	return nodes, edges, polys
	### YOUR CODE GOES ABOVE HERE ###

def build_nodes(world, polys):
	lines = set()
	nodes = set()
	tmp_pts = set()
	# get all obstacle ownership info
	obstacle = defaultdict(lambda: None)
	for o in world.getObstacles():
		for p in o.getPoints():
			obstacle[p] = o
	for poly in polys:
		if len(tmp_pts) > 1:
			nodes = nodes.union(tmp_pts)
		tmp_pts = set()
		for i in xrange(len(poly)):
			j = (i + 1) % len(poly)
			line = (poly[i], poly[j])
		 	midpoint = ((line[0][0] + line[1][0])/2, (line[0][1] + line[1][1])/2)
		 	a,b = line
		 	bad = False
		 	for o in world.getObstacles():
		 		if pointInsidePolygonPoints(midpoint, o.getPoints()):
		 			bad = True
		 	if bad:
		 		continue
		 	# if obstacle[a] != obstacle[b]:
			for p in tmp_pts:
				lines.add(tuple(sorted([midpoint, p])))
			tmp_pts.add(midpoint)
	return list(nodes), list(lines)

global_debug_world = None

def build_convex_hulls(world, agent = None):
	
	global global_debug_world
	global_debug_world = world
	polys = build_triangles(world, agent)

	# polys = merge_all_polys(world, polys)

	changed = True
	while changed:
		old_polys = polys[:]
		polys = merge_all_polys(world, polys)
		if len(old_polys) != len(polys):
			changed = True
		else:
			changed = False

	return polys



def merge_2_polys(a,b):
	line = tuple(commonPoints(a,b))
	if len(line) != 2:
		return None
	p1,p2 = line
	new_poly = []
	# enforce ordering p1 --> p2 --> rest_of_poly --> p1
	if a[(a.index(p1) + 1)%len(a)] != p2:
		p1, p2 = p2, p1
	ai = a.index(p2)
	bi = b.index(p1)

	while(a[ai] != p1):
		new_poly.append(a[ai])
		ai = (ai + 1) % len(a)
	while(b[bi] != p2):
		new_poly.append(b[bi])
		bi = (bi + 1) % len(b)
	# print(new_poly)
	return tuple(new_poly)

def merge_all_polys(world, polys):
	# return polys
	new_polys = set(polys)
	for pair in itertools.combinations(polys, 2):
		a,b = pair
		if a not in new_polys or b not in new_polys:
			continue
		if polygonsAdjacent(a,b):
			candidate = merge_2_polys(a,b)
			if candidate is not None and isConvex(candidate) and len(candidate) == len(a) + len(b) - 2:
				contains_obstacle = False
				for o in world.getObstacles():
					midpoint = [0,0]
					for p in o.getPoints():
						midpoint[0] += p[0]
						midpoint[1] += p[1]
					midpoint[0] = midpoint[0] / len(o.getPoints())
					midpoint[1] = midpoint[1] / len(o.getPoints())
					if pointInsidePolygonPoints(midpoint, candidate):
						contains_obstacle = True
				if contains_obstacle:
					continue
				new_polys.remove(a)
				new_polys.remove(b)
				new_polys.add(candidate)
	return list(new_polys)

def distVect(p1,p2):
	return (p1[0] - p2[0], p1[1] - p2[1])

def vect_len(v):
	return math.sqrt(v[0]**2 + v[1]**2)

def vect_angle(v1, v2):
	return math.acos(numpy.dot(v1, v2) / (vect_len(v1) * vect_len(v2)))

def isCW(poly):
    s = 0.0
    for i in xrange(0,len(poly)):
        v1 = poly[i];
        v2 = poly[(i + 1) % len(poly)]
        s += (v2[0]- v1[0]) * (v2[1] + v1[1]);
    return s > 0.0


# turns a polygon into a counter-clockwise poly starting with the most left and lowest node
def create_ordered_triangle(a,b,c):
	l = [a,b,c]
	l.sort()
	if not isCW(tuple(l)):
		return tuple(l)
	else:
		return (l[0], l[2], l[1])

def build_triangles(world, agent = None):
	# set up some useful structures
	hulls = set() 
	lines = world.getLines()
	connected = defaultdict(set)
	obstacle = defaultdict(lambda: None)
	pts = world.getPoints()

	# get connected into
	for l in lines:
		p1, p2 = l
		connected[p1].add(p2)
		connected[p2].add(p1)

	# get all obstacle ownership info
	for o in world.getObstacles():
		for p in o.getPoints():
			obstacle[p] = o
	# a = pts[0]		
	for a in pts:
		changed = True
		while changed: 
			changed = False
			# Get successors of a
			successors = connected[a].copy()
			for p2 in pts:
				if a is not p2 and rayTraceWorldNoEndPoints(a, p2, lines) == None:
					successors.add(p2)

			# check all pairwise permutations of successors
			for item in itertools.permutations(successors, 2):
				b, c = item

				# check that c is a successor of b
				if (c in connected[b]) or (rayTraceWorldNoEndPoints(b, c, lines) is None):
					# create the candidate triangle
					poly = create_ordered_triangle(a,b,c)

					# if it already exists then skip calculations
					if poly in hulls:
						continue
					
					# check if any obstacle is inside the candidate
					small_obs_inside = False
					for o in world.getObstacles():
						if o.getPoints()[0] not in set(poly) and pointInsidePolygonPoints(o.getPoints()[0], poly):
							small_obs_inside = True
					if small_obs_inside:
						continue

					# check if any of the lines are inside an obstacle
					new_lines = [(a,b),(b,c),(c,a)]
					line_inside_obstacle = False
					for l in new_lines:
						if obstacle[l[0]] == obstacle[l[1]] and l[1] not in connected[l[0]]:
							midpoint = ((l[0][0] + l[1][0])/2, (l[0][1] + l[1][1])/2)
							if pointInsidePolygonPoints(midpoint, obstacle[l[0]].getPoints()):
								line_inside_obstacle = True
					if line_inside_obstacle:
						continue

					if obstacle[poly[0]] == obstacle[poly[1]] and obstacle[poly[1]] == obstacle[poly[2]]:
						if len(obstacle[poly[0]].getPoints()) <= 3:
							continue

					# We found a valid triangle, add it to the hulls
					for l in new_lines:
						lines.append(l)
						connected[l[0]].add(l[1])
						connected[l[1]].add(l[0])
					hulls.add(poly)
					changed = True
					break
				# else:
				# 	drawCross(world.debug, item[0], color = (255, 0, 0), size = 15, width = 5)
				# 	drawCross(world.debug, item[1], color = (255, 0, 0), size = 15, width = 5)
				# 	print (b,c,connected[b],connected[c])
	return list(hulls)

def expand_line(line, change):
	source, target = line
	dx = target[0] - source[0]
	dy = target[1] - source[1]
	mag = float(math.sqrt(dx**2 + dy**2))
	dx, dy = dx / mag, dy / mag
	dx *= change
	dy *= change
	source = (source[0] - dx, source[1] - dy)
	target = (target[0] + dx, target[1] + dy)
	return (source, target)

def prune_edges(edges, world, agent):
	lines = []
	for line in edges:
		t_line = expand_line(line, agent.getMaxRadius())
		source, target = t_line
		if rayTraceWorld(source, target, world.getLines()) == None:
			valid = True
			for pt in world.getPoints():
				if (minimumDistance(line, pt) <= agent.getMaxRadius()):
					valid = False
					break
			if valid:
				lines.append(line)
	return lines

