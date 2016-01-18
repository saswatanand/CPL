module MovingAvg where

import CPLTI
-- import Prelude hiding ((>),(==),(<),(<=),(>=),(&&),(||))

movingAvg n p = \b ->
  let lastnbar = map ((.#) p)  [0..(n-1)]
  in ((sumM (map (\x -> x b) lastnbar)) /(toEnum n))

sumM [] = return (Just 0)
sumM (a:x) = do x' <- sumM x
                a' <- a
		case (x',a') of
		 (Just x1, Just a1) ->  return (Just (a1+x1))
		 _  -> return Nothing
		  

               
                

