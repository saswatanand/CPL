module ExpMovingAvg where
--default import start--
import IOExts
import CPLTI
import CPLPattern
import Prelude hiding ((>),(==),(<),(<=),(>=),(&&),(||))
import BuiltinPatterns
--default import end--

import MovingAvg

{- fast version with memoization, 
	but ugly i.e. user need use "mem 1 aux" and "readM 1" to do write and read-}

expMovingAvg n ind = mem 1 aux
 where 
  aux = \b ->
    if (b < n) then zeroProcess
	else if (b==n)
	      then (movingAvg n ind b)
	      else do p <- readM 1
		      c <- (ind b)
		      return (c*(2/((toEnum n)+1)) + (p .! 1)*(1-2/((toEnum n)+1)))


{-- original version i.e. very slow version
expMovingAvg n ind b
 = if (b < n)
     then return Nothing
     else if (b > n)
	      then (ind b)*(2/((toEnum n)+1)) + (expMovingAvg n (b-1))*(1-2/((toEnum n)+1))
	      else (movingAvg n ind b)
-}

{- not sure how to do it yet
fix f = f (fix f)
expMovingAvg = fix bigF
bigF f n ind b
 = if (b < n)
     then return Nothing
     else if (b > n)
	      then ((f n (b-1)) + 2) --(ind b)*(2/((toEnum n)+1)) + (1-2/((toEnum n)+1))*(f n (b-1))
	      else (movingAvg n ind b)
-}
