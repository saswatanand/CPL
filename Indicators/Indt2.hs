module Indt2 where
--default import start--
import IOExts
import CPLTI
import CPLPattern
import Prelude hiding ((>),(==),(<),(<=),(>=),(&&),(||))
import BuiltinPatterns
--default import end--

indt2::TI
indt2 = (close + low + high) /3