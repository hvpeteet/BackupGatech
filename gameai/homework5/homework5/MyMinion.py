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
from moba import *


global_minion_index = [0,0,0]
class MyMinion(Minion):
	global global_minion_index
	
	def __init__(self, position, orientation, world, image = NPC, speed = SPEED, viewangle = 360, hitpoints = HITPOINTS, firerate = FIRERATE, bulletclass = SmallBullet):
		Minion.__init__(self, position, orientation, world, image, speed, viewangle, hitpoints, firerate, bulletclass)
		self.states = [Idle, ShootEnemy, NavigateToNextTarget, Disperse, WaitForNMinions]
		### Add your states to self.states (but don't remove Idle)
		### YOUR CODE GOES BELOW HERE ###
		

		### YOUR CODE GOES ABOVE HERE ###

	def start(self):
		Minion.start(self)
		global global_minion_index
		self.index = global_minion_index[self.getTeam()]
		global_minion_index[self.getTeam()] += 1

		self.oponent_team = None
		# print(self.team)
		if self.getTeam() == 1:
			self.oponent_team = 2
		else:
			self.oponent_team = 1
		self.changeState(WaitForNMinions, 10)

class NavigateToNextTarget(State):

	def enter(self, oldstate):
		State.enter(self, oldstate)
		towers = self.agent.world.getTowersForTeam(self.agent.oponent_team)
		if self.agent.index % 2 == 0:
			towers.reverse()
		if towers is None or len(towers) == 0:
			self.target = self.agent.world.getBaseForTeam(self.agent.oponent_team)
		else:
			self.target = towers[0]
		if self.target is not None:
			self.agent.navigateTo(self.target.position)

	def execute(self, delta=0):
		State.execute(self, delta)
		if self.target is None:
			self.agent.changeState(Idle)
			return
		# if inTowerRange(self.agent, self.target):
		if inTowerRange(self.agent, self.target):
			self.agent.navigator.path = []
			self.agent.changeState(Disperse, self.target)


class WaitForNMinions(State):
	def enter(self, oldstate):
		# print "disperse"
		State.enter(self, oldstate)
		self.agent.stopMoving()

	def execute(self, delta=0):
		global global_minion_index
		if (global_minion_index[self.agent.getTeam()] > self.minion_target):
			self.agent.changeState(NavigateToNextTarget)

	def parseArgs(self, args):
		self.minion_target = args[0]

class Disperse(State):
	def enter(self, oldstate):
		State.enter(self, oldstate)
		self.target = None
		for dst in self.agent.getPossibleDestinations():
			if distance(dst, self.enemy.position) - self.enemy.getRadius() < BULLETRANGE:
				dist = distance(dst, self.agent.position)
				diste = distance(dst, self.enemy.position)
				if  dist < 200 and dist > 100 and diste > self.enemy.getRadius() + BULLETRANGE / 4:
					self.target = dst
					if (self.agent.index / 2) % 2 == 0:
						break
		if self.target is not None:
			rx, ry = random.uniform(0.0, 20.0), random.uniform(-20.0, 20.0)
			offsetx = pts2vect(self.target, self.enemy.position)
			offsety = orthogonal_vector(offsetx)
			offsetx, offsety = set_mag(offsetx, rx), set_mag(offsety, ry)
			offset = (offsetx[0] + offsety[0], offsetx[1] + offsety[1])
			self.target = pt_plus_vect(self.target, offset)
		else:
			offset = set_mag(pts2vect(self.enemy, self.position), BULLETRANGE - 10.0)
			self.target = pt_plus_vect(self.enemy.position, offset)
		self.agent.navigateTo(self.target)


	def execute(self, delta=0):
		State.execute(self, delta)
		if self.target is None:
			self.agent.stopMoving()
			self.agent.changeState(ShootEnemy, self.enemy)
			
		if self.agent.navigator.destination != self.target:
			self.agent.stopMoving()
			self.agent.changeState(ShootEnemy, self.enemy)

	def parseArgs(self, args):
		self.enemy = args[0]

class ShootEnemy(State):
	def parseArgs(self, args):
		self.enemy = args[0]

	def enter(self, oldstate):
		self.agent.stopMoving()
		self.agent.turnToFace(self.enemy.position)

	def execute(self, delta=0):
		if not inAgentRange(self.agent, self.enemy) or self.enemy.hitpoints <= 0:
			self.agent.changeState(NavigateToNextTarget)
			return None
		self.agent.shoot()



def inTowerRange(agent, tower):
	return distance(agent.position, tower.position) - agent.getMaxRadius() <= TOWERBULLETRANGE

def inAgentRange(agent, target):
	return distance(agent.position, target.position) - target.getRadius() <= BULLETRANGE 

def orthogonal_vector(v):
	return (v[1], -v[0])

def reverse_vector(v):
	return (-v[0], -v[1])

def set_mag(v, s):
	m = (v[0]**2 + v[1]**2)**0.5
	return (v[0] * s / m, v[1] * s / m)

def pts2vect(p1,p2):
	return (p2[0]-p1[0], p2[1]-p1[1])

def pt_plus_vect(pt, vect):
	return (pt[0] + vect[0], pt[1] + vect[1])

def scale_line(line, newsize):
	v = set_mag(pts2vect(line[0], line[1]), newsize)
	return (line[0], (line[1][0] + v[0], line[1][1] + v[1]))

def mag(vect):
	return (vect[0]**2 + vect[1]**2)**0.5

def bulletWillHitPoint(source, bullet, point, range, radius):
	if bullet is None or source is None or point is None:
		return False
	if bullet.position == source.position:
		return False
	extended_bullet_line = scale_line((source.position, bullet.position), range)
	if minimumDistance(extended_bullet_line, point) < radius:
		return True 
	return False

############################
### Idle
###
### This is the default state of MyMinion. The main purpose of the Idle state is to figure out what state to change to and do that immediately.

class Idle(State):
	
	def enter(self, oldstate):
		State.enter(self, oldstate)
		# stop moving
		self.agent.stopMoving()
		# base = self.agent.world.getBaseForTeam(self.agent.getTeam())
		# if base is not None:
		# 	print(base.numSpawned)
		
	def execute(self, delta = 0):
		State.execute(self, delta)
		### YOUR CODE GOES BELOW HERE ###

		### YOUR CODE GOES ABOVE HERE ###
		return None

##############################
### Taunt
###
### This is a state given as an example of how to pass arbitrary parameters into a State.
### To taunt someome, Agent.changeState(Taunt, enemyagent)

class Taunt(State):

	def parseArgs(self, args):
		self.victim = args[0]

	def execute(self, delta = 0):
		if self.victim is not None:
			print "Hey " + str(self.victim) + ", I don't like you!"
		self.agent.changeState(Idle)

##############################
### YOUR STATES GO HERE:

