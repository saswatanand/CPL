module BuiltinPatterns where

import CPLBasic
import CPLTI
import CPLPattern
import Prelude hiding ((>),(==),(<),(<=),(>=),(&&),(||))
--		       (+),(-),(*),(/),abs,signum,fromInteger,fromRational)


up, down, hill, hs, hns :: Pattern
up = (Up,\xs -> [])
down =  (Down, \xs -> [])


hill = (up >.> down) ?? (\[a,b,c] -> 
				[close b > close a,
				 close b > close c])

hs = (hill >.> hill >.> hill) ?? (\[a,b,c,d,e,f,g]->
	     let e0_ = (close .# 1) a
		 e0 = close a
		 e1 = close b
		 e2 = close c
		 e3 = close d
		 e4 = close e
	  	 e5 = close f
		 e6 = close g
		 e6_ = (close .# 1) g
		 x = (e1 + e5) 
 	     in [e1 > e0 && e1 >e2,
		 e3 > e1 && e3 > e5,
		 e5 > e4 && e5 > 46])

hns = (up >.> ( down >.> ( up >.> ( down >.> ( up >.> down )))))
	?? (\[a,b,c,d,e,f,g] ->
	     let	
		 e0_ = (close .# 1) a
		 e0 = close a
		 e1 = close b
		 e2 = close c
		 e3 = close d
		 e4 = close e
	  	 e5 = close f
		 e6 = open g
		 e6_ = (open .# 1) g
		 x = (e1 + e5) / 2
		 x1 = abs (e1-x)
		 x2 = abs (e5-x)
		 y = (e2 + e4) / 2
		 y1 = abs (e2 - y)
		 y2 = abs (e4 - y)
		 z1 = (((e1-e2)+(e5-e4))/ 2) / (e3-(e2+e4)/2)	
		 z2 = (e3-(e2+e4)/2)/e3
		 z3 = (cast (f - b)) / 4.0
		 m = e4 - e2
		 a0 = (m*((cast(a-c))/(cast(e-c)))) + e2
                 g0 = (m*((cast(g-c))/(cast(e-c)))) + e2
             in [
		 -- (R2) --
		 e3 > e1,
		 -- (R3) --
		 e3 > e5,
		 -- (R4a) --		 
		 x1 <= (0.04 * x),	-- R4a	Allowable difference btw the PRICES of the two shoulders (e1 and e5)
		 x2 <= (0.04 * x),	-- R4a	The bigger it is, the more the heights of two shoulders can differ
		 -- (R5a) --
		 y1 <= (0.04 * y),	-- R5a	Allowable difference btw the PRICES of the two bottoms (e2 and e4)
		 y2 <= (0.04 * y),	-- R5a	The bigger it is, the steeper is the neckline		 
		 -- (R6) --
		 z1 <= 0.7,
		 -- R6	Upper bound on proportion of both shoulder heights to head height from neckline
		 -- (R7) --
		 z1 >= 0.25,		
		 -- R7	Lower bound on proportion of both shoulder heights to head height from neckline
		 -- (R8) --
		 z2 >= 0.03,
		 ------------ R9 --------------
		 (abs ((cast(c-b))-z3)) <= (1.2 * z3),
		 (abs ((cast(d-c))-z3)) <= (1.2 * z3),
		 (abs ((cast(e-d))-z3)) <= (1.2 * z3),
		 (abs ((cast(f-e))-z3)) <= (1.2 * z3),
		 ------------------------------	Rules out extreme horizontal asymmetries
		 (e0 > a0) && (e0_ < a0),
		 (e6 < g0) && (e6_ > g0) -- Constraints on start and end points
 		])
			   		 