module Lib where

import Array

div2 n = if (n==2) then 1 else  1+(div2 (n-2))

splitL xs = let n=length xs
	    in if (even n) then splitAt (div2 n) xs
			 else splitAt (div2 (n-1)) xs

merge [] [] = []
merge [] y = y
merge x [] = x
merge ((i,a):x) ((j,b):y) = if (i<j) then (i,a):(merge x ((j,b):y))
				else (j,b):(merge ((i,a):x) y)

-- tupMergeSort sorts the list according to element's index
tupMergeSort [] = []
tupMergeSort [x] = [x]
tupMergeSort xs = let (a,b) = splitL xs
		      a' = tupMergeSort a 
		      b' = tupMergeSort b
		  in merge a' b'

mergeE [] y = y
mergeE x [] = x
mergeE ((i,a):x) ((j,b):y) = if (a<b) then (i,a):(merge x ((j,b):y))
				else (j,b):(merge ((i,a):x) y)

-- elemSort sorts a list according to its elements.
elemSort [] = []
elemSort [x] = [x]
elemSort xs = let (a,b) = splitL xs
		  a' = elemSort a
		  b' = elemSort b
	      in mergeE a' b'

------------------- some functions in Haskell -------------------
dynamicSort olist xs = let xs' = zip olist xs
		       in (map snd (tupMergeSort xs'))

ordmap1 olist f xs = map (\i-> f (xs !! (i-1))) olist

ordmap2 olist f xs = 
     let xs' =ordmap1 olist f xs
	 lss = zip olist xs'
         lss' = tupMergeSort lss
     in map snd lss'

-- sort takes a list of Int eg. [2,2,4,3,5] and return [0,1,3,2,4]
sort xs = let idxlist = [0..(length xs -1)]
	  in map fst (elemSort (zip idxlist xs))

ordlss olist res = map (\i -> res !! i) olist
		     
------------------- end of functions in Haskell -----------------
{-
class AB a b where
 f1 :: a -> b

instance AB Int Int where
 f1 x = x+1

instance AB Bool Bool where
 f1 x = not x

m1 f = let a = f 5
	   b = f False
       in if b then f else f
-}